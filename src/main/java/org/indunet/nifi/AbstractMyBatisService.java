package org.indunet.nifi;

import java.lang.reflect.InvocationTargetException;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
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
import org.apache.nifi.reporting.InitializationException;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Tags({"nifi", "mybatis", "database", "autowire"})
@CapabilityDescription("Provides database access through mybatis, processors use it as controller services.")
public abstract class AbstractMyBatisService extends AbstractControllerService {
    protected ComponentLog log;
    private Configuration configuration;

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
        DBCPService dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);

        TransactionFactory factory = new JdbcTransactionFactory();
        DataSource dataSource = new DbcpServiceAdapter(dbcpService);
        // Field field = null;

//        try {
//            field = dbcpService.getClass().getDeclaredField("dataSource");
//            field.setAccessible(true);
//
//            dataSource = (DataSource) field.get(dbcpService);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            this.log.info("Fail getting data source from dbcp service.");
//            this.log.info(e.getMessage());
//
//            dataSource = new DbcpServiceAdapter(dbcpService);
//        }

        Environment environment = new Environment("nifi-mybatis", factory, dataSource);
        configuration = new Configuration(environment);
        this.initialize(configuration);
    }

    protected void autowire(Object object) {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        Field[] fields = object.getClass().getDeclaredFields();

        // Check before autowire.
        Arrays.stream(fields)
                .forEach(field -> {
                        try (SqlSession session = sqlSessionFactory.openSession()) {
                            // Throw org.apache.ibatis.binding.BindingException when there is no class found.
                            session.getMapper(field.getType());
                        } catch (BindingException e) {
                            e.printStackTrace();
                            this.log.error(e.getMessage());
                            this.log.error(field.getType().getName() + " is not known to the MapperRegistry.");
                        }

                });

        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .filter(field -> field.getType().isInterface())
                .peek(field -> field.setAccessible(true))
                .forEach(field -> {
                    Object mapperProxy = Proxy.newProxyInstance(
                            sqlSessionFactory.getClass().getClassLoader(),
                            new Class[]{field.getType()},
                            (proxy, method, args) -> {
                                try(SqlSession session = sqlSessionFactory.openSession()) {
                                    // Throw org.apache.ibatis.binding.BindingException when there is no class found.
                                    Object mapper = session.getMapper(field.getType());
                                    Object value = method.invoke(mapper, args);

                                    session.commit();
                                    session.close();

                                    return value;
                                } catch (IllegalAccessException | IllegalArgumentException
                                            | InvocationTargetException | BindingException e) {
                                    this.log.error(e.getMessage());
                                    e.printStackTrace();

                                    throw new InitializationException(e);
                                }
                            }
                    );

                    try {
                        field.set(object, mapperProxy);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        this.log.error(e.getMessage());
                    }
                });
    }

    protected abstract void initialize(Configuration configuration);

    @OnDisabled
    public void shutdown() {

    }
}
