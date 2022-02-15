package com.penguinwebstudio.building;

import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

public class AddForm {
	
	private String aEmail;
	
	// Name
	@NotBlank
	@Size(min=1, max=200)
	@Pattern(regexp="^[A-Za-z0-9\\- \\.,'\\(\\)]+$")
	private String aPhone;
	
	// Occupancy
	@NotNull
	@Min(1)
	@Max(9999)
	private Integer aFirstName;
	
	// Type
	@NotBlank
	@Size(min=1, max=50)
	@Pattern(regexp="^[A-Za-z0-9 \\-\\$]+$")
	private String aWebsite;
	
	// Tiles
	@NotBlank
	@Size(min=1, max=3)
	@Pattern(regexp="^[A-Za-z0-9]+$")
	private String aLastName;

	// Style
	@NotBlank
	@Size(min=1, max=50)
	@Pattern(regexp="^[A-Za-z0-9 ]+$")
	private String aCreated;
	
	// Last Updated
	@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private Date aAddress;
	
	public String getaEmail() {
		return this.aEmail;
	}
	
	public String getaPhone() {
		return this.aPhone;
	}
	
	public Integer getaFirstName() {
		return this.aFirstName;
	}
	
	public String getaWebsite() {
		return this.aWebsite;
	}
	
	public String getaLastName() {
		return this.aLastName;
	}
	
	public String getaCreated() {
		return this.aCreated;
	}
	
	public Date getaAddress() {
		return this.aAddress;
	}
	
	public void setaEmail(String email) {
		this.aEmail = email;
	}
	
	public void setaPhone(String name) {
		this.aPhone = name;
	}
	
	public void setaFirstName(Integer occupancy) {
		this.aFirstName = occupancy;
	}
	
	public void setaWebsite(String type) {
		this.aWebsite = type;
	}
	
	public void setaLastName(String tiles) {
		this.aLastName = tiles;
	}
	
	public void setaCreated(String style) {
		 this.aCreated = style;
	}
	
	public void setaAddress(Date lastUpdated) {
		this.aAddress = lastUpdated;
	}

}