package models;

import java.util.ArrayList;

/**
 * 
 * @author tmack
 *
 */

import java.util.Arrays;

/*
 * A representation of a student candidate
 */
public class Candidate {
	
	String name; 
	String university; 
	double GPA;
	String[] leadership;
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
		this.leadership = null;
		this.degree = null;
		this.keywords = new ArrayList();
		this.organizations = new ArrayList();
		this.email = null;
	}
	public Candidate(String name, String university, double GPA, String[] leadership) {
		this.name = name;
		this.university = university;
		this.GPA = GPA;
		this.leadership = leadership;
	}
	
	public void prettyPrint() {
		System.out.println("Resume Path:" + this.resumeFilePath);
		if(this.getName() != null) {
			System.out.println(this.getName());
		}
		if(this.getEmail() != null) {
			System.out.println(this.getEmail());
		}
	}


	@Override
	public String toString() {
		return "Candidate [name=" + name + ", university=" + university + ", GPA=" + GPA + ", leadership="
				+ Arrays.toString(leadership) + ", email=" + email + ", degree=" + degree + ", keywords=" + keywords
				+ ", organizations=" + organizations + ", resumeFilePath=" + resumeFilePath + "]";
	}
	public String getName() {
		return name;
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
	public String[] getLeadership() {
		return leadership;
	}
	public void setLeadership(String[] leadership) {
		this.leadership = leadership;
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
		organizations = organizations;
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
	
	
	
	

}
