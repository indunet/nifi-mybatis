package org.indunet.nifi;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.indunet.nifi.mapper.VehicleMapper;
import org.indunet.nifi.service.VehicleService;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.config.JtaTransactionManagerFactoryBean;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NiFiApplicationContextTest {
    static protected NiFiApplicationContextTest niFiApplicationContext;
    public GenericApplicationContext applicationContext;

    public NiFiApplicationContextTest() {
        this.applicationContext = new GenericApplicationContext();
        new XmlBeanDefinitionReader(applicationContext).loadBeanDefinitions("nifi-spring.xml");
    }

    synchronized public static NiFiApplicationContextTest get() {
        if (niFiApplicationContext == null) {
            niFiApplicationContext = new NiFiApplicationContextTest();
        }

        return niFiApplicationContext;
    }

    public void registerDataSource(DataSource dataSource) {
        // this.applicationContext.get
    }

    public void registerMyBatis() {

    }

    public void registerTransactionManager() {

    }

    @Test
    public void testSpring() throws Exception {
        NiFiApplicationContextTest context = NiFiApplicationContextTest.get();

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Manager.class);
        // BeanDefinitionBuilder.genericBeanDefinition()

//        for (Object arg : args) {
//            beanDefinitionBuilder.addConstructorArgValue(arg);
//        }

         beanDefinitionBuilder.addAutowiredProperty("employee");
         beanDefinitionBuilder.addPropertyReference("employee", "employee");
        beanDefinitionBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);

        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) context.applicationContext.getBeanFactory();

//         Man aMan = new Man("Brother Chun");
//         Supplier<Man> supplier = () -> new Man("123");

        // beanFactory.registerBeanDefinition("man", BeanDefinitionBuilder.genericBeanDefinition(Man.class, () -> aMan).getRawBeanDefinition());
        beanFactory.registerBeanDefinition("employee", BeanDefinitionBuilder.genericBeanDefinition(Employee.class).getRawBeanDefinition());
        beanFactory.registerBeanDefinition("manager", beanDefinition);


//        System.out.println("Init spring context");

        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://dell-node-06:5432/chance-om");
        dataSource.setUsername("postgres");
        dataSource.setPassword("123456");

        beanFactory.registerBeanDefinition("dataSource",
                BeanDefinitionBuilder.genericBeanDefinition(DataSource.class, () -> dataSource).getRawBeanDefinition());

        // TransactionFactory factory = new JdbcTransactionFactory();

        // Environment environment = new Environment("nifi-mybatis", factory, dataSource);
        // Configuration configuration = new Configuration(environment);
        // configuration.addMappers("org.indunet.nifi.mapper");
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);

        // beanFactory.registerBeanDefinition("configuration", BeanDefinitionBuilder.genericBeanDefinition(Configuration.class, () -> configuration).getRawBeanDefinition());
        // SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

//        MapperScannerConfigurer mapper = new MapperScannerConfigurer();
//        mapper.set

        BeanDefinitionBuilder sqlSessionFactory = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class);
        sqlSessionFactory.addPropertyReference("dataSource", "dataSource");
        // sqlSessionFactory.addPropertyValue("configuration", configuration);
        // sqlSessionFactory.addPropertyValue("mapperLocations", "classpath*:org/indunet/nifi/mapper/*.xml");

         beanFactory.registerBeanDefinition("sqlSessionFactory", sqlSessionFactory.getBeanDefinition());
        // factoryBean.set

        MapperScannerConfigurer scanner = new MapperScannerConfigurer();
        scanner.setBasePackage("org.indunet.nifi.mapper");
        scanner.setSqlSessionFactoryBeanName("sqlSessionFactory");
        beanFactory.registerBeanDefinition("scanner",
                BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class, () -> scanner).getRawBeanDefinition());

        // DataSourceTransactionManager

        // Transaction
        BeanDefinitionBuilder transactionManager = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
        transactionManager.addConstructorArgReference("dataSource");
        beanFactory.registerBeanDefinition("transactionManager", transactionManager.getBeanDefinition());

//        BeanDefinitionBuilder jtaTransactionManager = BeanDefinitionBuilder.genericBeanDefinition(JtaTransactionManagerFactoryBean.class);
//        beanFactory.registerBeanDefinition("jtaTransactionManager", jtaTransactionManager.getBeanDefinition());


//        SqlSessionTemplate template = new SqlSessionTemplate(factoryBean.getObject());
//        beanFactory.registerBeanDefinition("sqlSessionTemplate", BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class, () -> template).getRawBeanDefinition());

//        BeanDefinitionBuilder vehicleMapper = BeanDefinitionBuilder.genericBeanDefinition(MapperFactoryBean.class);
//        vehicleMapper.addPropertyValue("mapperInterface", "org.indunet.nifi.mapper.VehicleMapper");
//        vehicleMapper.addPropertyReference("sqlSessionFactory", "sqlSessionFactory");
//        beanFactory.registerBeanDefinition("vehicleMapper", vehicleMapper.getBeanDefinition());
//
//        BeanDefinitionBuilder vehicleModelMapper = BeanDefinitionBuilder.genericBeanDefinition(MapperFactoryBean.class);
//        vehicleModelMapper.addPropertyValue("mapperInterface", "org.indunet.nifi.mapper.VehicleModelMapper");
//        vehicleModelMapper.addPropertyReference("sqlSessionFactory", "sqlSessionFactory");
//        beanFactory.registerBeanDefinition("vehicleModelMapper", vehicleModelMapper.getBeanDefinition());

        VehicleService vehicleService = new VehicleService();
        BeanDefinitionBuilder vehicleServiceBuilder = BeanDefinitionBuilder.genericBeanDefinition(VehicleService.class, () -> vehicleService);
        vehicleServiceBuilder.setAutowireMode(2);

        // beanFactory.registerBeanDefinition("vehicleService", BeanDefinitionBuilder.genericBeanDefinition(VehicleService.class).getRawBeanDefinition());
        beanFactory.registerBeanDefinition("vehicleService", vehicleServiceBuilder.getRawBeanDefinition());

        context.applicationContext.refresh();

//        ConfigurableListableBeanFactory factory = context.applicationContext.getBeanFactory();
//        AspectJAwareAdvisorAutoProxyCreator aspectJPostProcessor = new AspectJAwareAdvisorAutoProxyCreator();
//        aspectJPostProcessor.setBeanFactory(factory);
//        aspectJPostProcessor.setProxyClassLoader(context.applicationContext.getClassLoader());
//        factory.addBeanPostProcessor(aspectJPostProcessor);

        // VehicleMapper vehicleMapper = context.applicationContext.getBean(VehicleMapper.class);
        System.out.println(context.applicationContext.getBeanDefinitionNames().length);
        Arrays.stream(context.applicationContext.getBeanDefinitionNames()).forEach(System.out::println);

        System.out.println(context.applicationContext.getBean(VehicleMapper.class).listVehicle().size());
        System.out.println(context.applicationContext.getBean(VehicleService.class).count());

        // VehicleService vehicleService = context.applicationContext.getBean(VehicleService.class);
        VehicleService vehicleService2 = context.applicationContext.getBean(VehicleService.class);

        Manager manager = context.applicationContext.getBean("manager", Manager.class);
        manager.print();

        IntStream.range(0, 10)
                .parallel().forEach(i -> {
            vehicleService2.saveVehicle();
            // vehicleService.saveVehicleModel();
        });
    }
}