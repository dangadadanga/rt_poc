package org.teachforamerica.models;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;

import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * 
 * @author tmack
 *
 */


/*
 * A representation of a student candidate
 */
public class Prospect {
	
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
	
	
	public Prospect(String resumeFilePath) {
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
	public Prospect(String name, String university, double GPA, ArrayList<Role> roles) {
		this.name = name;
		this.university = university;
		this.GPA = GPA;
		this.roles = roles;
	}
	
	public void print() {
		System.out.println("Extracted content for" + this.resumeFilePath);
		System.out.println(this.toString());
		this.printRoles();
	}


	@Override
	public String toString() {
		return "\nname=" + name + ", \nuniversity=" + university + ", \nGPA=" + GPA
				+ ", \nemail=" + email + ", \ndegree=" + degree + ", \nkeywords=" + keywords;
	}
	
	public String toJSON() {
			try {
				
				XContentBuilder builder = jsonBuilder()
					    .startObject()
					        .field("name", this.getName())
					        .field("university", this.getUniversity())
					        .field("gpa", this.getGPA())
					        .field("email", this.getEmail())
					        .field("resume-filepath", this.getResumeFilePath())
						    .array("organizations", this.getOrganizations().toArray())
					       
					    .endObject();
					return builder.string();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
			
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
			System.out.println("Organization: " + role.getOrganization() + ", Title: " +role.getRole());
			
		}
	}
	
	
	

}
