package org.teachforamerica.controllers;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.Splitter;;


public class BulkLoader {
	
	public boolean loadResumeBook(String resumeBookFilePath, String filePathToSave, String resumeFileName) {
		PDDocument document;	
        ArrayList<ArrayList<Integer>> resumebook = new ArrayList<ArrayList<Integer>>();
	
		try {
			document = PDDocument.load(resumeBookFilePath);
			Splitter splitter = new Splitter();
			PDFTextStripper pdfStripper = new PDFTextStripper();
			List<PDDocument> splittedDocuments = splitter.split(document);
			boolean resumesStarted = false;
			int i = 0;
			
			System.out.println("Splitting resume book into individual resumes");
			for(PDDocument page: splittedDocuments) {
				 pdfStripper.setStartPage(1);
		            pdfStripper.setEndPage(1);
		            String parsedText = pdfStripper.getText(page);
		            
//		            if(i==21) {
//		            	System.out.println(page.getDocumentInformation().getDictionary().toString());
//		            	System.out.println(parsedText);
//		            }
		            if(parsedText.contains("@") && 
		            		(parsedText.contains(".com") || parsedText.contains(".COM") || parsedText.contains(".edu")|| parsedText.contains(".net")|| parsedText.contains(".org"))) {
		            	
		            	// create a new resume
		            	resumebook.add(new ArrayList<Integer>());
		            	resumebook.get(resumebook.size()-1).add(i);
		            	resumesStarted = true;
		            	
		            } else {
		            	
		            	if(resumesStarted) {
		            	// append page to resume
		            	resumebook.get(resumebook.size()-1).add(i);
		            	
		            	
		            	}
		            }
				i++;
				
			}
			ArrayList<PDDocument> resumesToExport = new ArrayList<PDDocument>();
			
			for(ArrayList<Integer> resume: resumebook) {
				PDDocument newResume;
				if(resume.size() > 1) {

					newResume = splittedDocuments.get(resume.get(0));
					
					for(i =1; i < resume.size() ; i++) {
					
						newResume.addPage((PDPage) splittedDocuments.get(resume.get(i)).getDocumentCatalog().getAllPages().get(0));
					}
	
					resumesToExport.add(newResume);
					
				} else {
					resumesToExport.add(splittedDocuments.get(resume.get(0)));
				}
				
			}
			
		
			i = 1;
			
			System.out.println("Saving individual resumes..");
			for(PDDocument resume: resumesToExport) {
				String filepath = filePathToSave +resumeFileName+ i++ +".pdf";
				File file = new File(filepath); 
				file.getParentFile().mkdirs();
				if(file.exists() == false) { 
					file.createNewFile(); 
				}
				try {
					resume.save(filepath);
					resume.close();
				} catch (COSVisitorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			System.out.println("Saved " + i + " resumes in " + filePathToSave + " directory");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	

}
