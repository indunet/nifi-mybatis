package org.indunet.nifi.controller;

import org.apache.ibatis.session.Configuration;
import org.indunet.nifi.AbstractMyBatisService;
import org.indunet.nifi.Autowired;
import org.indunet.nifi.entity.VehicleDO;
import org.indunet.nifi.entity.VehicleModelDO;
import org.indunet.nifi.mapper.VehicleMapper;
import org.indunet.nifi.mapper.VehicleModelMapper;

import java.util.List;

public class VehicleControllerService extends AbstractMyBatisService implements VehicleService {
    @Autowired
    VehicleMapper vehicleMapper;
    @Autowired
    VehicleModelMapper vehicleModelMapper;

    @Override
    protected void initialize(Configuration conf) {
        conf.addMappers("org.indunet.nifi.mapper");

        this.autowire(this);
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
