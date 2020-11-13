package org.indunet.nifi.entity;


public class VehicleModelDO {
  private long id;
  private long manufacturerId;
  private String nameCn;
  private String nameEn;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getManufacturerId() {
    return manufacturerId;
  }

  public void setManufacturerId(long manufacturerId) {
    this.manufacturerId = manufacturerId;
  }

  public String getNameCn() {
    return nameCn;
  }

  public void setNameCn(String nameCn) {
    this.nameCn = nameCn;
  }


  public String getNameEn() {
    return nameEn;
  }

  public void setNameEn(String nameEn) {
    this.nameEn = nameEn;
  }

}
