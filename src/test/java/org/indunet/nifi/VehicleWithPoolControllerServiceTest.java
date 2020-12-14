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
import org.indunet.nifi.controller.VehicleWithPoolControllerService;
import org.indunet.nifi.mapper.VehicleMapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class VehicleWithPoolControllerServiceTest {
    protected final static String DATABASE_URL = "jdbc:postgresql://dell-node-06:5432/nifi-mybatis";
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

        VehicleWithPoolControllerService vehicleWithPoolService = new VehicleWithPoolControllerService();

        runner.addControllerService("vehicleService", vehicleWithPoolService);

        runner.setProperty(vehicleWithPoolService, VehicleWithPoolControllerService.DATABASE_URL, DATABASE_URL);
        runner.setProperty(vehicleWithPoolService, VehicleWithPoolControllerService.DB_USER, DB_USER);
        runner.setProperty(vehicleWithPoolService, VehicleWithPoolControllerService.DB_PASSWORD, DB_PASSWORD);
        runner.setProperty(vehicleWithPoolService, VehicleWithPoolControllerService.DB_DRIVERNAME, DB_DRIVERNAME);

        runner.enableControllerService(vehicleWithPoolService);

        vehicleWithPoolService.listVehicleModel();
//        assertTrue(vehicleWithPoolService.listVehicle().size() > 0);
//        assertTrue(vehicleWithPoolService.listVehicleModel().size() >= 0);
    }
}
