package org.indunet.nifi;

import org.indunet.nifi.mapper.VehicleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author
 * @version 1.0
 */
public class Manager {
    @Autowired
    Employee employee;
    @Autowired
    VehicleMapper vehicleMapper;

    boolean value = false;

    @Transactional
    public void print() {
        System.out.println(this.employee.name);
        System.out.println(vehicleMapper.listVehicle().size());
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
