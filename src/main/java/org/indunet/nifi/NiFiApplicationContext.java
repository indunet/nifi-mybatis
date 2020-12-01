package org.indunet.nifi;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.sql.DataSource;
import java.util.function.Supplier;

public class NiFiApplicationContext {
    static protected NiFiApplicationContext niFiApplicationContext;
    public GenericApplicationContext applicationContext;

    protected NiFiApplicationContext() {
        this.applicationContext = new GenericApplicationContext();
    }

    synchronized public static NiFiApplicationContext get() {
        if (niFiApplicationContext == null) {
            niFiApplicationContext = new NiFiApplicationContext();
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

    public static void main(String[] args) throws Exception {
        NiFiApplicationContext context = NiFiApplicationContext.get();

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Manager.class);
        // BeanDefinitionBuilder.genericBeanDefinition()

//        for (Object arg : args) {
//            beanDefinitionBuilder.addConstructorArgValue(arg);
//        }

        // beanDefinitionBuilder.addAutowiredProperty("employee");
        // beanDefinitionBuilder.addPropertyReference("employee", "employee");
        beanDefinitionBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);

        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) context.applicationContext.getBeanFactory();

        Man aMan = new Man("Brother Chun");
        // Supplier<Man> supplier = () -> new Man("123");

        beanFactory.registerBeanDefinition("man", BeanDefinitionBuilder.genericBeanDefinition(Man.class, () -> aMan).getRawBeanDefinition());
        beanFactory.registerBeanDefinition("employee", BeanDefinitionBuilder.genericBeanDefinition(Employee.class).getRawBeanDefinition());
        beanFactory.registerBeanDefinition("manager", beanDefinition);

        context.applicationContext.refresh();
        Manager manager = context.applicationContext.getBean("manager", Manager.class);
        Man man = context.applicationContext.getBean("man", Man.class);
        man.print();
        manager.print();
        System.out.println("Init spring context");

        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://dell-node-06:5432/chance-om");
        dataSource.setUsername("postgres");
        dataSource.setPassword("123456");

        beanFactory.registerBeanDefinition("dataSource", BeanDefinitionBuilder.genericBeanDefinition(DataSource.class, () -> dataSource).getRawBeanDefinition());

        TransactionFactory factory = new JdbcTransactionFactory();
        Environment environment = new Environment("nifi-mybatis", factory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMappers("org.indunet.nifi.mapper");

        beanFactory.registerBeanDefinition("configuration", BeanDefinitionBuilder.genericBeanDefinition(Configuration.class, () -> configuration).getRawBeanDefinition());

        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfiguration(configuration);
        // factoryBean.set

        SqlSessionTemplate template = new SqlSessionTemplate(factoryBean.getObject());
        beanFactory.registerBeanDefinition("sqlSessionTemplate", BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class, () -> template).getRawBeanDefinition());


    }
}