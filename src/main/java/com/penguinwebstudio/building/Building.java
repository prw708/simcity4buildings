package com.penguinwebstudio.building;

import java.util.Base64;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class Building {
	
	@Id
	private Long id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="occupancy")
	private Integer occupancy;
	
	@Column(name="type")
	private String type;
	
	@Column(name="tiles")
	private String tiles;

	@Column(name="style")
	private String style;
	
	@Column(name="lastUpdated")
	private Date lastUpdated;
	
	@Column(name="image")
	private byte[] image;
	
	@Column(name="approved")
	private boolean approved;
	
	@Column(name="deletion")
	private boolean deletion;
	
	protected Building() {
	}
	
	public Building(Long id, String name, Integer occupancy, String type, String tiles, String style) {
		this.id = id;
		this.name = name;
		this.occupancy = occupancy;
		this.type = type;
		this.tiles = tiles;
		this.style = style;
		this.lastUpdated = new Date();
		this.image = null;
		this.approved = false;
		this.deletion = false;
	}
	
	public String binaryImageToString() {
		if (this.image == null) {
			return "";
		}
		return Base64.getEncoder().encodeToString(this.image);
	}
	
	public Long getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Integer getOccupancy() {
		return this.occupancy;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getTiles() {
		return this.tiles;
	}
	
	public String getStyle() {
		return this.style;
	}
	
	public Date getLastUpdated() {
		return this.lastUpdated;
	}
	
	public byte[] getImage() {
		return this.image;
	}
	
	public boolean getApproved() {
		return this.approved;
	}
	
	public boolean getDeletion() {
		return this.deletion;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setOccupancy(Integer occupancy) {
		this.occupancy = occupancy;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setTiles(String tiles) {
		this.tiles = tiles;
	}
	
	public void setStyle(String style) {
		this.style = style;
	}
	
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	public void setImage(byte[] image) {
		this.image = image;
	}
	
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	
	public void setDeletion(boolean deletion) {
		this.deletion = deletion;
	}
	
}
