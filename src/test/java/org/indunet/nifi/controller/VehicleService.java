package org.indunet.nifi.controller;

import org.apache.nifi.controller.ControllerService;
import org.indunet.nifi.entity.VehicleDO;
import org.indunet.nifi.entity.VehicleModelDO;
import org.indunet.nifi.mapper.VehicleMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VehicleService extends ControllerService {
    @Transactional
    List<VehicleModelDO> listVehicleModel();
    @Transactional
    List<VehicleDO> listVehicle();
}
