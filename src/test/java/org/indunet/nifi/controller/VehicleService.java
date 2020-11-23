package org.indunet.nifi.controller;

import org.apache.nifi.controller.ControllerService;
import org.indunet.nifi.entity.VehicleDO;
import org.indunet.nifi.entity.VehicleModelDO;
import org.indunet.nifi.mapper.VehicleMapper;

import java.util.List;

public interface VehicleService extends ControllerService {
    List<VehicleModelDO> listVehicleModel();
    List<VehicleDO> listVehicle();
}
