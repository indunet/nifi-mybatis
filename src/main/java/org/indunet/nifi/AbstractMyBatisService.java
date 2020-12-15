package org.indunet.nifi;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.logging.ComponentLog;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * This is a abstract class, developers can realize user-defined controller service through inheritance it.
 * Compared to AbstractMyBatisWithDBCPService, AbstractMyBatisService don't have a built-in connection pool, developers need to pass in DBCPService as a property.
 *
 * @see AbstractMyBatisWithDBCPService
 */
@Tags({"nifi", "database", "mybatis", "spring"})
@CapabilityDescription("Provides database access through mybatis, processors use it as controller services.")
public abstract class AbstractMyBatisService extends AbstractControllerService {
    protected ComponentLog log;
    protected GenericApplicationContext applicationContext;
    protected BeanDefinitionRegistry registry;

    public static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection of database.")
            .required(true)
            .identifiesControllerService(DBCPService.class)
            .build();

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return Collections.unmodifiableList(Collections.singletonList(DBCP_SERVICE));
    }

    @Override
    public void init(ControllerServiceInitializationContext context) {
        this.log = context.getLogger();
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) {
        this.applicationContext = new GenericApplicationContext();

        new XmlBeanDefinitionReader(applicationContext).loadBeanDefinitions("nifi-spring.xml");
        this.registry = (BeanDefinitionRegistry) this.applicationContext.getBeanFactory();

        // DataSource
        DBCPService dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);
        DBCPServiceAdapter dbcpServiceAdapter = new DBCPServiceAdapter(dbcpService);

        BeanDefinitionBuilder dataSourceBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DBCPServiceAdapter.class, () -> dbcpServiceAdapter);
        registry.registerBeanDefinition("dataSource", dataSourceBuilder.getRawBeanDefinition());

        // SqlSessionFactory
        BeanDefinitionBuilder sqlSessionFactory = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class);
        sqlSessionFactory.addPropertyReference("dataSource", "dataSource");
        registry.registerBeanDefinition("sqlSessionFactory", sqlSessionFactory.getRawBeanDefinition());

        // DataSourceTransactionManager
        BeanDefinitionBuilder transactionManager = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
        transactionManager.addConstructorArgReference("dataSource");
        registry.registerBeanDefinition("transactionManager", transactionManager.getBeanDefinition());

        this.initialize(new Configuration());
    }

    /**
     * This is a configuration class that helps users to initialize controller service through chain expression.
     */
    public class Configuration {
        Object controllerService;
        String basePackage;

        /**
         * Sets controller service.
         *
         * @param controllerService usually pass in this
         * @return this
         */
        public Configuration setControllerService(Object controllerService) {
            this.controllerService = controllerService;

            return this;
        }

        /**
         * Sets base package of mybatis which including xml files.
         *
         * @param basePackage the base package
         * @return this
         */
        public Configuration setBasePackage(String basePackage) {
            this.basePackage = basePackage;

            return this;
        }

        /**
         * After the parameters above are configured, call this method to complete the initialization of controller service.
         */
        public void build() {
            // MapperScannerConfigurer
            BeanDefinitionBuilder scannerConfigurerBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            scannerConfigurerBuilder.addPropertyValue("basePackage", this.basePackage);
            scannerConfigurerBuilder.addPropertyValue("sqlSessionFactoryBeanName", "sqlSessionFactory");
            registry.registerBeanDefinition("mapperScannerConfigurer", scannerConfigurerBuilder.getRawBeanDefinition());

            // Spring services
            Arrays.stream(this.controllerService.getClass().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(Autowired.class))
                    .filter(f -> !f.getType().isInterface())
                    .filter(f -> f.getType().isAnnotationPresent(Service.class))
                    .forEach(f -> {
                        BeanDefinitionBuilder serviceBuilder =
                                BeanDefinitionBuilder.genericBeanDefinition(f.getType());
                        serviceBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
                        registry.registerBeanDefinition(f.getType().getSimpleName(),
                                serviceBuilder.getRawBeanDefinition());
                    });

            // ControllerService
            Supplier supplier = () -> controllerService;

            BeanDefinitionBuilder controllerServiceBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(controllerService.getClass(), supplier);
            controllerServiceBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
            registry.registerBeanDefinition(controllerService.getClass().getSimpleName(),
                    controllerServiceBuilder.getRawBeanDefinition());

            applicationContext.refresh();
        }
    }

    /**
     * Developers need to rewrite the abstract method to complete the initialization.
     *
     * @param configuration Configure parameters and initialize through it.
     */
    protected abstract void initialize(Configuration configuration);

    @OnDisabled
    public void shutdown() {
        this.applicationContext.close();
    }
}
