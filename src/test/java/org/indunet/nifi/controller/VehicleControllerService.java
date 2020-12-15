package org.indunet.nifi.controller;

import org.indunet.nifi.AbstractMyBatisService;
import org.indunet.nifi.entity.VehicleDO;
import org.indunet.nifi.entity.VehicleModelDO;
import org.indunet.nifi.mapper.VehicleMapper;
import org.indunet.nifi.mapper.VehicleModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class VehicleControllerService extends AbstractMyBatisService implements VehicleService {
    @Autowired
    VehicleMapper vehicleMapper;
    @Autowired
    VehicleModelMapper vehicleModelMapper;

    @Override
    protected void initialize(Configuration conf) {
        conf.setBasePackage("org.indunet.nifi.mapper")
                .setControllerService(this)
                .build();
    }

    @Override
    public List<VehicleDO> listVehicle() {
        return this.vehicleMapper.listVehicle();
    }

    @Override
    public List<VehicleModelDO> listVehicleModel() {
        return this.vehicleModelMapper.listVehicleModel();
    }
}
