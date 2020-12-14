package org.indunet.nifi.service;

import org.indunet.nifi.mapper.VehicleMapper;
import org.indunet.nifi.mapper.VehicleModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author
 * @version 1.0
 */
@Service
public class VehicleService {
    @Autowired
    VehicleModelMapper vehicleModelMapper;
    @Autowired
    VehicleMapper vehicMapper;

    @Transactional
    public int count() {
        return
            this.vehicleModelMapper.listVehicleModel().size()
                    + this.vehicMapper.listVehicle().size();
    }

    @Transactional
    public void saveVehicle() {
        this.vehicMapper.save();
        this.vehicMapper.save();
        this.vehicleModelMapper.save();
        this.vehicleModelMapper.save();
        this.vehicleModelMapper.save();

        // throw new NullPointerException();
    }

    @Transactional
    public void saveVehicleModel() {
        this.vehicleModelMapper.save();
        this.vehicleModelMapper.save();
    }
}
