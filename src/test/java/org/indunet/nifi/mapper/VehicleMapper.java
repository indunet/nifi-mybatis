package org.indunet.nifi.mapper;

import org.indunet.nifi.entity.VehicleDO;

import java.util.List;

public interface VehicleMapper {
    List<VehicleDO> listVehicle();
}