package org.indunet.nifi.mapper;

import org.indunet.nifi.entity.VehicleModelDO;

import java.util.List;

public interface VehicleModelMapper {
    List<VehicleModelDO> listVehicleModel();
    int save();
}