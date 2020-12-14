package org.indunet.nifi;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.dbcp.DBCPValidator;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
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

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Deng Ran
 * @version 1.0
 */
@Tags({"nifi", "mybatis", "database", "autowire", "connection pool"})
@CapabilityDescription("Provides database access through mybatis, processors use it as controller services.")
public abstract class AbstractMyBatisWithPoolService extends AbstractControllerService {
    protected ComponentLog log;
    protected GenericApplicationContext applicationContext;
    protected BeanDefinitionRegistry registry;

    public static final PropertyDescriptor DATABASE_URL = new PropertyDescriptor.Builder()
            .name("Database Connection URL")
            .description("A database connection URL used to connect to a database. May contain database system name, host, port, database name and some parameters."
                    + " The exact syntax of a database connection URL is specified by your DBMS.")
            .defaultValue(null)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    public static final PropertyDescriptor DB_DRIVERNAME = new PropertyDescriptor.Builder()
            .name("Database Driver Class Name")
            .description("Database driver class name")
            .defaultValue(null)
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    public static final PropertyDescriptor DB_DRIVER_LOCATION = new PropertyDescriptor.Builder()
            .name("database-driver-locations")
            .displayName("Database Driver Location(s)")
            .description("Comma-separated list of files/folders and/or URLs containing the driver JAR and its dependencies (if any). For example '/var/tmp/mariadb-java-client-1.1.7.jar'")
            .defaultValue(null)
            .required(false)
            .addValidator(StandardValidators.createListValidator(true, true, StandardValidators.createURLorFileValidator()))
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    public static final PropertyDescriptor DB_USER = new PropertyDescriptor.Builder()
            .name("Database User")
            .description("Database user name")
            .defaultValue(null)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    public static final PropertyDescriptor DB_PASSWORD = new PropertyDescriptor.Builder()
            .name("Password")
            .description("The password for the database user")
            .defaultValue(null)
            .required(false)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        List<PropertyDescriptor> list = new ArrayList<>();

        list.add(DATABASE_URL);
        list.add(DB_DRIVERNAME);
        list.add(DB_DRIVER_LOCATION);
        list.add(DB_USER);
        list.add(DB_PASSWORD);

        return Collections.unmodifiableList(list);
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
        BeanDefinitionBuilder dataSourceBuilder = BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
        dataSourceBuilder.addPropertyValue("url", context.getProperty(DATABASE_URL).getValue());
        dataSourceBuilder.addPropertyValue("driverClassName", context.getProperty(DB_DRIVERNAME).getValue());
        dataSourceBuilder.addPropertyValue("username", context.getProperty(DB_USER).getValue());
        dataSourceBuilder.addPropertyValue("password", context.getProperty(DB_PASSWORD).getValue());
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

    public class Configuration {
        Object controllerService;
        String basePackage;

        public Configuration setControllerService(Object controllerService) {
            this.controllerService = controllerService;

            return this;
        }

        public Configuration setBasePackage(String basePackage) {
            this.basePackage = basePackage;

            return this;
        }

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
                    .filter(f -> f.getType().isInterface() == false)
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

    protected abstract void initialize(Configuration configuration);

    @OnDisabled
    public void shutdown() {
        this.applicationContext.close();
    }
}
