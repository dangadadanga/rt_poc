package models;

public class Role {
	
	String role = null;
	String organization = null;
	String weight = null;
	
	public Role(String organization) {
		super();
		this.organization = organization;
	}
	public Role(String role, String organization, String weight) {
		super();
		this.role = role;
		this.organization = organization;
		this.weight = weight;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
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
