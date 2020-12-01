package org.indunet.nifi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author
 * @version 1.0
 */
public class Manager {
    @Autowired
    Employee employee;

    boolean value = false;

    @Transactional
    public void print() {
        System.out.println(this.employee.name);
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
