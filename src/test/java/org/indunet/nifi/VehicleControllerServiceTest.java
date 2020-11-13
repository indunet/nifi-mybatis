package org.indunet.nifi;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPConnectionPool;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.indunet.nifi.controller.VehicleControllerService;
import org.indunet.nifi.controller.VehicleService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class VehicleControllerServiceTest {
    protected final static String DATABASE_URL = "jdbc:postgresql://dell-node-06:5432/chance-om";
    protected final static String DB_USER = "postgres";
    protected final static String DB_PASSWORD = "123456";
    protected final static String DB_DRIVERNAME = "org.postgresql.Driver";

    public class TestProcessor extends AbstractProcessor {
        @Override
        public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        }

        @Override
        protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
            List<PropertyDescriptor> propDescs = new ArrayList<>();
            propDescs.add(new PropertyDescriptor.Builder()
                    .name("Vehicle Service")
                    .description("Vehicle Service")
                    .identifiesControllerService(VehicleService.class)
                    .required(true)
                    .build());

            return propDescs;
        }
    }

    @Test
    public void testOnConfigured() throws InitializationException {
        TestRunner runner = TestRunners.newTestRunner(new TestProcessor());

        VehicleControllerService vehicleService = new VehicleControllerService();
        DBCPConnectionPool dbcpService = new DBCPConnectionPool();

        runner.addControllerService("vehicleService", vehicleService);
        runner.addControllerService("dbcpService", dbcpService);

        runner.setProperty(dbcpService, DBCPConnectionPool.DATABASE_URL, DATABASE_URL);
        runner.setProperty(dbcpService, DBCPConnectionPool.DB_USER, DB_USER);
        runner.setProperty(dbcpService, DBCPConnectionPool.DB_PASSWORD, DB_PASSWORD);
        runner.setProperty(dbcpService, DBCPConnectionPool.DB_DRIVERNAME, DB_DRIVERNAME);
        runner.setProperty(vehicleService, VehicleControllerService.DBCP_SERVICE, "dbcpService");
        runner.setProperty("Vehicle Service", "vehicleService");

        runner.enableControllerService(dbcpService);
        runner.enableControllerService(vehicleService);

        assertTrue(vehicleService.listVehicle().size() > 0);
        assertTrue(vehicleService.listVehicleModel().size() > 0);
    }
}
