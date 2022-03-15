package com.penguinwebstudio.building;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RemoveAdditionForm {
	
	private String aPhone;
	
	@NotBlank
	@Size(min=1, max=10)
	@Pattern(regexp="^[0-9]+$")
	String buildingId;
	
	@NotBlank 
	@Size(min=1, max=1000) 
	@Pattern(regexp="^[A-Za-z0-9\\-_]+$")
	String recaptcha;
	
	public void setaPhone(String data) {
		this.aPhone = data;
	}
	
	public String getaPhone() {
		return this.aPhone;
	}
	
	public void setBuildingId(String id) {
		this.buildingId = id;
	}
	
	public String getBuildingId() {
		return this.buildingId;
	}
	
	public void setRecaptcha(String recaptcha) {
		this.recaptcha = recaptcha;
	}
	
	public String getRecaptcha() {
		return this.recaptcha;
	}
	
}