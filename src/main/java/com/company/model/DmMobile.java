package com.company.model;

import com.mongodb.ReflectionDBObject;

public class DmMobile extends ReflectionDBObject {

	private String Key;// 号码段，如：1801623
	private String Prov;// 省
	private String City;// 城市
	private String Type;// 中国电信
	private Long Ut = System.currentTimeMillis();// 更新时间
	
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		this.Key = key;
	}
	public String getProv() {
		return Prov;
	}
	public void setProv(String prov) {
		this.Prov = prov;
	}
	public String getCity() {
		return City;
	}
	public void setCity(String city) {
		this.City = city;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		this.Type = type;
	}
	public Long getUt() {
		return Ut;
	}
	public void setUt(Long ut) {
		Ut = ut;
	}
	
}
