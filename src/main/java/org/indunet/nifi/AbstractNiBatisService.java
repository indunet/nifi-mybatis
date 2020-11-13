package org.indunet.nifi;

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

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Tags({"nifi", "mybatis", "database", "autowire"})
@CapabilityDescription("Provides database access through mybatis, processors use it as controller services.")
public abstract class AbstractNiBatisService extends AbstractControllerService {
    protected ComponentLog log;
    private Configuration configuration;

    public static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection of database.")
            .required(true)
            .identifiesControllerService(DBCPService.class)
            .build();

    private static final List<PropertyDescriptor> properties =
            Collections.unmodifiableList(Arrays.asList(new PropertyDescriptor[] {DBCP_SERVICE}));

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public void init(ControllerServiceInitializationContext context) {
        this.log = context.getLogger();
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) {
        DBCPService dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);

        TransactionFactory factory = new JdbcTransactionFactory();
        DataSource dataSource = null;
        Field field = null;

        try {
            field = dbcpService.getClass().getDeclaredField("dataSource");
            field.setAccessible(true);

            dataSource = (DataSource) field.get(dbcpService);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            this.log.error(e.getMessage());

            dataSource = new DbcpServiceAdapter(dbcpService);
        }

        Environment environment = new Environment("nifi-mybatis", factory, dataSource);
        configuration = new Configuration(environment);
        this.initialize(configuration);
    }

    protected void autowire(Object object) {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        Field[] fields = object.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .filter(field -> field.getType().isInterface())
                .forEach(field -> {
                    field.setAccessible(true);
                    Object mapperProxy = Proxy.newProxyInstance(
                            sqlSessionFactory.getClass().getClassLoader(),
                            new Class[]{field.getType()},
                            (proxy, method, args) -> {
                                SqlSession session = sqlSessionFactory.openSession();

                                Object mapper = session.getMapper(field.getType());

                                if (mapper == null) {
                                    this.log.error("The mapper of " + mapper.getClass().getName() + " doesn't exists.");
                                    return null;
                                }

                                Object value = method.invoke(session.getMapper(field.getType()), args);

                                session.commit();
                                session.close();

                                return value;
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
