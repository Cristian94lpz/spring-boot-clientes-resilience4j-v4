package com.example.clientes.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PostalCode {

    private String city;
    private String province;
    @JsonProperty("postal_code")
    private String postalCode;
    @JsonProperty("area_code")
    private String areaCode;
    private String timezone;
    private String lat;
    private String lon;

    public PostalCode() {
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PostalCode that = (PostalCode) o;
        return Objects.equals(city, that.city) && Objects.equals(province, that.province) && Objects.equals(postalCode, that.postalCode) && Objects.equals(areaCode, that.areaCode) && Objects.equals(timezone, that.timezone) && Objects.equals(lat, that.lat) && Objects.equals(lon, that.lon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, province, postalCode, lat, lon);
    }

    @Override
    public String toString() {
        return "PostalCode{" +
                "city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", timezone='" + timezone + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                '}';
    }
}
