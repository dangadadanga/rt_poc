package models;

import java.util.ArrayList;

/**
 * 
 * @author tmack
 *
 */


/*
 * A representation of a student candidate
 */
public class Candidate {
	
	String name; 
	String university; 
	double GPA;
	ArrayList<Role> roles;
	String email;
	

	// TODO: include Ethnicity
	String degree;
	ArrayList<String> keywords = null;
	ArrayList<String> organizations = null;
	String resumeFilePath;
	
	
	public Candidate(String resumeFilePath) {
		this.resumeFilePath = resumeFilePath;
		this.name = null;
		this.university = null;
		this.GPA = 0.0;
		this.roles = new ArrayList<Role>();
		this.degree = null;
		this.keywords = new ArrayList<String>();
		this.organizations = new ArrayList<String>();
		this.email = null;
	}
	public Candidate(String name, String university, double GPA, ArrayList<Role> roles) {
		this.name = name;
		this.university = university;
		this.GPA = GPA;
		this.roles = roles;
	}
	
	public void prettyPrint() {
		System.out.println("Extracted content for" + this.resumeFilePath);
		System.out.println(this.toString());
		this.printRoles();
	}


	@Override
	public String toString() {
		return "\nname=" + name + ", \nuniversity=" + university + ", \nGPA=" + GPA
				+ ", \nemail=" + email + ", \ndegree=" + degree + ", \nkeywords=" + keywords;
	}
	public String getName() {
		return name;
	}
	
	public ArrayList<Role> getRoles() {
		return roles;
	}
	public void setRoles(ArrayList<Role> roles) {
		this.roles = roles;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUniversity() {
		return university;
	}
	public void setUniversity(String university) {
		this.university = university;
	}
	public double getGPA() {
		return GPA;
	}
	public void setGPA(double gPA) {
		GPA = gPA;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public ArrayList<String> getKeywords() {
		return keywords;
	}
	public void setKeywords(ArrayList<String> keywords) {
		this.keywords = keywords;
	}
	public ArrayList<String> getOrganizations() {
		return organizations;
	}
	public void setOrganizations(ArrayList<String> organizations) {
		this.organizations = organizations;
	}
	public String getResumeFilePath() {
		return resumeFilePath;
	}
	public void setResumeFilePath(String resumeFilePath) {
		this.resumeFilePath = resumeFilePath;
	}
	public String getDegree() {
		return degree;
	}
	public void setDegree(String degree) {
		this.degree = degree;
	}
	
	public void printRoles() {
		for(Role role: this.getRoles()) {
			System.out.println("Organization: " + role.getOrganization() + ", Role: " +role.getRole());
			
		}
	}
	
	
	

}
