package org.indunet.nifi.entity;

public class VehicleDO {
    private long id;
    private long vehicleModelId;
    private String vehicleCode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVehicleModelId() {
        return vehicleModelId;
    }

    public void setVehicleModelId(long vehicleModelId) {
        this.vehicleModelId = vehicleModelId;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }
}
