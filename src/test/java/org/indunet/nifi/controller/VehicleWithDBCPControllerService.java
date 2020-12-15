package org.indunet.nifi.controller;

import org.indunet.nifi.AbstractMyBatisWithDBCPService;
import org.indunet.nifi.entity.VehicleDO;
import org.indunet.nifi.entity.VehicleModelDO;
import org.indunet.nifi.mapper.VehicleMapper;
import org.indunet.nifi.mapper.VehicleModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class VehicleWithDBCPControllerService extends AbstractMyBatisWithDBCPService implements VehicleService {
    @Autowired
    VehicleMapper vehicleMapper;
    @Autowired
    VehicleModelMapper vehicleModelMapper;
    @Autowired
    org.indunet.nifi.service.VehicleService vehicleService;

    @Override
    protected void initialize(Configuration conf) {
        conf.setBasePackage("org.indunet.nifi.mapper")
                .setControllerService(this)
                .build();
    }

    @Override
    public List<VehicleModelDO> listVehicleModel() {
        vehicleService.saveVehicle();
        // throw new NullPointerException();

        return this.vehicleModelMapper.listVehicleModel();
    }

    @Override
    @Transactional
    public List<VehicleDO> listVehicle() {
        return this.vehicleMapper.listVehicle();
    }
}
