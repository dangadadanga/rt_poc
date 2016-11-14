package org.teachforamerica.models;

public class Role {
	
	String title = null;
	String organization = null;
	String weight = null;
	
	public Role(String organization) {
		super();
		this.organization = organization;
	}
	public Role(String role, String organization, String weight) {
		super();
		this.title = role;
		this.organization = organization;
		this.weight = weight;
	}
	public String getRole() {
		return title;
	}
	public void setRole(String role) {
		this.title = role;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	
	

}
