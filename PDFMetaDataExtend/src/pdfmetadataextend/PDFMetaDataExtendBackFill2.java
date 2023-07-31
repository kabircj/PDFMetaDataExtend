
/**
 * Copyright (C) 

This program is free software: you can redistribute it and/or modify it under the terms 
of the GNU Affero General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU Affero General Public License for more details.
 *
 * Author: Kabir Jakkamsetti (k.jakkamsetti@ieee.org)
 */
package pdfmetadataextend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.xml.xmp.DublinCoreSchema;
import com.lowagie.text.xml.xmp.XmpArray;
import com.lowagie.text.xml.xmp.XmpWriter;


public class PDFMetaDataExtendBackFill2 {
	
	private static final String EXPORT_PATH_BASE = "Z:\\PDF-METADATA"; 
	String dateFolderName = "";
	File newDateFolder = null;
	
	/*
	 * public PDFMetaDataExtendBackFill2() {
	 * PropertyConfigurator.configure("log4j.configuration"); }
	 */
	
	public static void main (String[] args){
		try {			
			PDFMetaDataExtendBackFill2 instance = new PDFMetaDataExtendBackFill2();
			instance.executeActionForNewItems(args[0], args[1], args[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static Vector getAuthorTerms(String authorKeywords) {
        Vector terms = new Vector();
		for (String authorKeywordStr : authorKeywords.split("~")) {
			terms.addElement(authorKeywordStr);
		}       
		System.out.println("terms.size::"+terms.size());
        return terms;
    }
    
    public static Vector getAuthors(String delimitedAuthors) {
    	
        Vector authors = new Vector();
		for (String authorStr : delimitedAuthors.split("~")) {
			authors.addElement(authorStr);
		}
		System.out.println("authors.size::"+authors.size());
        return authors;
    }   
     
    
	private void executeActionForNewItems(String creationIntervalHrs, String batchSize, String pdfFolderName) {

		System.out.println("creationIntervalHrs: " + creationIntervalHrs);
		System.out.println("batchSize: " + batchSize);
		System.out.println("pdfFolderName: " + pdfFolderName);
		
		int creationIntervalInt =  0;
		if(!creationIntervalHrs.trim().equalsIgnoreCase("")){
			try {
				creationIntervalInt = Integer.parseInt(creationIntervalHrs);
			} catch (NumberFormatException e1) {
				System.out.println(" NumberFormatException parsing creationIntervalHrs");
				e1.printStackTrace();
			}
		}
		int batchsize = 100;
		try {
			batchsize = Integer.valueOf(batchSize);
		} catch (NumberFormatException ne) {
			System.out.println(" NumberFormatException parsing batchsize");
			ne.printStackTrace();
		}
		
		File pdfFolder = new File(pdfFolderName);
		if(!pdfFolder.exists()){
			System.out.println(" pdfFolder does not exist!");
			return;
		}


		try {
			Date creationTimeJava = null;
			Date currentDate = new Date();
			System.out.println("EXPORT_PATH_BASE::"+EXPORT_PATH_BASE);				
			File[] pdfFiles = pdfFolder.listFiles(new pdfFilenameFilter());
			
			if(pdfFiles.length == 0 ){
				System.out.println("No PDF's to process");
				return;
			}


			for (int i=0; i< pdfFiles.length; i++)       {
				File pdfFile = pdfFiles[i];
				String pdfFilePath = pdfFile.getAbsolutePath();	
				String pdfFileName = pdfFile.getName();	
				long hoursDiff = 0;
				
				if (creationIntervalInt > 0) {					
					creationTimeJava = new Date(pdfFile.lastModified());
					hoursDiff = hoursDifference(currentDate, creationTimeJava);
				}

				if ((creationIntervalInt == 0) || (hoursDiff <= creationIntervalInt)) {
					if (!isNullOrBlank(pdfFileName) ) {
							try {
								addPDFMetadata(pdfFilePath);
							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}
							System.out.println("Added PDF metadata for file::" + pdfFileName);	
					} else {
						System.out.println("pdfFileName is null or blank. Skipping... ");
					}	
				} else {
					System.out.println("CreationInterval=0 or hoursDiff <= creationIntervalInt. Skipping article .... " );
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
    
    private void addPDFMetadata(String inputPDFFilePath) throws Exception{

		PdfReader reader = null;
		PdfReader reader1 = null;
		PdfReader reader2 = null;
		PdfReader reader3 = null;
		com.itextpdf.kernel.pdf.PdfReader reader7 = null;
		FileOutputStream fos = null;
		ByteArrayOutputStream baos = null;
		FileOutputStream fos2 = null;
		ByteArrayOutputStream os = null;
		PdfWriter writer = null;
		PdfDocument outputPDF = null;
		PdfStamper stamper = null;
		PdfStamper stamper1 = null;
		XmpWriter xmp1 = null;
		XmpWriter xmp = null;
		String artAMSid = "10834";	

		String outFileName1 = "";
		String outFileName2 = "";
		String outFileName3 = "";
		String outFileName4 = "";
		
        try {
    		String name = artAMSid + ".pdf";
			
			String artTitle = "Extracting sentence groups based on Bayes classifier by multi-features";
			String artNonNormNames = "Junwei Han; Xiang Ji; Xintao Hu; Lei Guo; Tianming Liu";
			String pubFullTitle = "IEEE Transactions on Medical Imaging";
			String artOrigPubYear = "2018";
			String artVolNum = "1";
			String artIssNum = "2";
			String artDOI = "10.1109/cp.2015.0240";
			String artAuthKeywords = "Arousal recognition; affective computing; fMRI-derived features; multimodal DBM";
			String pubPublishers = "IEEE";
			String artIssPart = "2";
			String artStartPg = "1";
			String artEndPgNum = "10";
			String artOrigPubDay = "3";
			String artOrigPubMonth = "8";
			String pubConfStartDay = "1";
			String pubConfStartMonth = "6";
			String pubConfStartYr = "2018";
			String pubConfEndDay = "4";
			String pubConfEndMonth = "7";
			String pubConfEndYr = "2018";
			String pubAMSid = "10891";
			String issAMSid = "10833";           
			String publisher = "";      
			//String authNames = "";
			//String keywords = "";
			
			if(isNullOrBlank(artTitle)) artTitle = " ";
			if(isNullOrBlank(pubFullTitle)) pubFullTitle = " ";
			if(isNullOrBlank(artOrigPubYear) || artOrigPubYear.equalsIgnoreCase("0")) artOrigPubYear = " ";
			if(isNullOrBlank(artVolNum)) artVolNum = " ";
			if(isNullOrBlank(artIssNum)) artIssNum = " ";
			if(isNullOrBlank(artDOI)) artDOI = " ";            
			if(isNullOrBlank(artIssPart)) artIssPart = " ";
			if(isNullOrBlank(artStartPg)) artStartPg = " ";
			if(isNullOrBlank(artEndPgNum)) artEndPgNum = " ";
			if(isNullOrBlank(artOrigPubDay) || artOrigPubDay.equalsIgnoreCase("0")) artOrigPubDay = " ";
			if(isNullOrBlank(artOrigPubMonth)) artOrigPubMonth = " ";
			if(isNullOrBlank(pubConfStartDay) || pubConfStartDay.equalsIgnoreCase("0")) pubConfStartDay = " ";
			if(isNullOrBlank(pubConfStartMonth)) pubConfStartMonth = " ";
			if(isNullOrBlank(pubConfStartYr) || pubConfStartYr.equalsIgnoreCase("0")) pubConfStartYr = " ";
			if(isNullOrBlank(pubConfEndDay) || pubConfEndDay.equalsIgnoreCase("0")) pubConfEndDay = " ";
			if(isNullOrBlank(pubConfEndMonth)) pubConfEndMonth = " ";
			if(isNullOrBlank(pubConfEndYr) || pubConfEndYr.equalsIgnoreCase("0")) pubConfEndYr = " ";
			if(isNullOrBlank(pubAMSid)) pubAMSid = " ";
			if(isNullOrBlank(issAMSid)) issAMSid = " ";
			if(isNullOrBlank(artAMSid)) artAMSid = " ";
			
			if(isNullOrBlank(pubPublishers)) 
				publisher = " ";
			else
				publisher = pubPublishers.split("~")[0];
			
			if(isNullOrBlank(artNonNormNames)) 
				artNonNormNames = "  ";
			
			if(isNullOrBlank(artAuthKeywords)) 
				artAuthKeywords = "  ";
			
			outFileName1 = inputPDFFilePath.substring(0, inputPDFFilePath.lastIndexOf("."))+ "_output1.pdf";
			outFileName2 = inputPDFFilePath.substring(0, inputPDFFilePath.lastIndexOf("."))+ "_output2.pdf";
			outFileName3 = inputPDFFilePath.substring(0, inputPDFFilePath.lastIndexOf("."))+ "_output3.pdf";
			outFileName4 = inputPDFFilePath.substring(0, inputPDFFilePath.lastIndexOf("."))+ "_output4.pdf";
            
			reader7 = new com.itextpdf.kernel.pdf.PdfReader(inputPDFFilePath);
			//PdfDocument outputPDF = new PdfDocument(reader7) ; 
			reader7.setUnethicalReading(true);
			writer = new PdfWriter(new File(outFileName1));
			
			outputPDF = new PdfDocument(reader7, writer);
			outputPDF.getTrailer().getAsDictionary(PdfName.Info).remove(PdfName.Author);
			outputPDF.getTrailer().getAsDictionary(PdfName.Info).remove(PdfName.Keywords);
			outputPDF.getTrailer().getAsDictionary(PdfName.Info).remove(PdfName.Subject);
			outputPDF.getTrailer().getAsDictionary(PdfName.Info).remove(PdfName.Title);
			outputPDF.close();
			
			
			reader1 = new PdfReader(outFileName1);
			removeExistingDocInfo(reader1, outFileName2);		
			

			reader = new PdfReader(outFileName2);
			char pdfMinorVersionChar = reader.getPdfVersion();
			int pdfVer = 5;
			if (Character.isDigit(pdfMinorVersionChar))
				pdfVer = Character.getNumericValue(pdfMinorVersionChar);

			System.out.println("PDF version:: " + pdfVer + " for " + artAMSid);


			reader3 = new PdfReader(outFileName2);
			fos = new FileOutputStream(outFileName3);
			stamper1 = new PdfStamper(reader3, fos);
			HashMap<String, String> info = reader3.getInfo();
			reader3.close();

			if(pdfVer == 0)
				if(!isNullOrBlank(artNonNormNames)) info.put("Author",artNonNormNames.replace('~', ';'));
			
			if (pdfVer > 0) {
				if(!isNullOrBlank(artTitle)) info.put("Title", artTitle);
				if(!isNullOrBlank(pubFullTitle) || !isNullOrBlank(artOrigPubYear) || !isNullOrBlank(artVolNum) || !isNullOrBlank(artIssNum) || !isNullOrBlank(artDOI)) 
					info.put("Subject",	pubFullTitle + ";" + artOrigPubYear + ";" + artVolNum + ";" + artIssNum + ";" + artDOI);
			}
			if(pdfVer > 0 && pdfVer < 4){
				if(!isNullOrBlank(artNonNormNames)) info.put("Author",artNonNormNames.replace('~', ';'));
				if(!isNullOrBlank(artAuthKeywords)) info.put("Keywords",artAuthKeywords.replace('~', ';'));		
			}

			if (pdfVer > 0) {
				String meetingStartDate = "";
				String meetingEndDate = "";
				if(" ".equalsIgnoreCase(pubConfStartDay) && " ".equalsIgnoreCase(pubConfStartMonth) && " ".equalsIgnoreCase(pubConfStartYr))
					meetingStartDate = " ";
				else 
					meetingStartDate = pubConfStartDay + " " + pubConfStartMonth + " " + pubConfStartYr;
				
				if(" ".equalsIgnoreCase(pubConfEndDay) && " ".equalsIgnoreCase(pubConfEndMonth) && " ".equalsIgnoreCase(pubConfEndYr))
					meetingEndDate = " ";
				else 
					meetingEndDate = pubConfEndDay + " " + pubConfEndMonth + " " + pubConfEndYr;
				
				//System.out.println("pubConfStartDay::"+pubConfStartDay + ", pubConfStartMonth::"+pubConfStartMonth + ", pubConfStartYr::"+pubConfStartYr);
				
				if(!isNullOrBlank(meetingStartDate)) info.put("Meeting Starting Date", meetingStartDate);
				else System.out.println("meetingStartDate is blank skipping....");
				if(!isNullOrBlank(meetingEndDate)) info.put("Meeting Ending Date", meetingEndDate);
				if(!isNullOrBlank(pubAMSid)) info.put("IEEE Publication ID", pubAMSid);
				if(!isNullOrBlank(issAMSid)) info.put("IEEE Issue ID", issAMSid);
				if(!isNullOrBlank(artAMSid)) info.put("IEEE Article ID", artAMSid);
			}

			stamper1.setMoreInfo(info);
			baos = new ByteArrayOutputStream();

			xmp1 = new XmpWriter(baos, info);
			xmp1.close();
			stamper1.setXmpMetadata(baos.toByteArray());
			stamper1.close();

			reader2 = new PdfReader(outFileName3);
			fos2 = new FileOutputStream(outFileName4);
			stamper = new PdfStamper(reader2, fos2);

			os = new ByteArrayOutputStream();
			xmp = new XmpWriter(os);

			DublinCoreSchema dc = new DublinCoreSchema();
			if (pdfVer >= 4) {
				if(!isNullOrBlank(artAuthKeywords)){
					XmpArray subject = new XmpArray(XmpArray.ORDERED);
					Vector terms = getAuthorTerms(artAuthKeywords);
					for (int i = 0; i < terms.size(); i++) {
						String term = (String) terms.elementAt(i);
						subject.add(term);
					}
					if (terms.size() > 0)
						dc.setProperty(DublinCoreSchema.SUBJECT, subject);
				}
				
				if(!isNullOrBlank(artNonNormNames)){
					XmpArray creator = new XmpArray(XmpArray.ORDERED);
					Vector authors = getAuthors(artNonNormNames);
					for (int i = 0; i < authors.size(); i++) {
						String author = (String) authors.elementAt(i);
						creator.add(author);
					}

					if (authors.size() > 0)
						dc.setProperty(DublinCoreSchema.CREATOR, creator);
				}
			}

			if (pdfVer >= 4){
				if(!isNullOrBlank(publisher)) dc.addPublisher(publisher);
				if(!isNullOrBlank(artTitle)) dc.addTitle(artTitle);
				if(!isNullOrBlank(pubFullTitle) || !isNullOrBlank(artOrigPubYear)  || !isNullOrBlank(artVolNum) || !isNullOrBlank(artIssNum) || !isNullOrBlank(artDOI)) 
					dc.addDescription(pubFullTitle + ";" + artOrigPubYear + ";" + artVolNum + ";" + artIssNum + ";" + artDOI);
			}

			xmp.addRdfDescription(dc);

			if (pdfVer >= 4) {
				Prism21Schema prism = new Prism21Schema();
				if(!isNullOrBlank(pubFullTitle)) prism.setProperty(Prism21Schema.PUBLICATIONNAME, pubFullTitle);
				if(!isNullOrBlank(artVolNum)) prism.setProperty(Prism21Schema.VOLUME, artVolNum);
				if(!isNullOrBlank(artIssNum)) prism.setProperty(Prism21Schema.ISSUE_IDENTIFIER, artIssNum);
				if(!isNullOrBlank(artIssPart)) prism.setProperty(Prism21Schema.NUMBER, artIssPart);
				if(!isNullOrBlank(artDOI)) prism.setProperty(Prism21Schema.DOI, artDOI);
				if(!isNullOrBlank(artStartPg)) prism.setProperty(Prism21Schema.STARTING_PAGE, artStartPg);
				if(!isNullOrBlank(artEndPgNum)) prism.setProperty(Prism21Schema.ENDING_PAGE, artEndPgNum);
				if(!isNullOrBlank(artOrigPubDay)  || !isNullOrBlank(artOrigPubMonth) || !isNullOrBlank(artOrigPubYear) ) 
					prism.setProperty(Prism21Schema.COVER_DISPLAY_DATE, artOrigPubDay + " " + artOrigPubMonth + " " + artOrigPubYear);           
			    xmp.addRdfDescription(prism);
			}

			xmp.close();
			stamper.setXmpMetadata(os.toByteArray());
			stamper.close();
			

			
			File outFile4 = new File(outFileName4);
			if(outFile4.exists())

			outFile4.renameTo( new File(EXPORT_PATH_BASE + File.separator + dateFolderName + File.separator + name ));


			closeObjects(writer, reader, reader1, reader2, reader3, reader7, outputPDF, 
					baos, os, fos, fos2, stamper, stamper1, xmp, xmp1);
            
        } catch(Exception e) {
			closeObjects(writer, reader, reader1, reader2, reader3, reader7, outputPDF, 
					baos, os, fos, fos2, stamper, stamper1, xmp, xmp1);	
			e.printStackTrace();
			throw(e);				
        } finally{
        	File inputFile = new File(inputPDFFilePath);
        	File outFile1 = new File(outFileName1);
        	File outFile2 = new File(outFileName2);
        	File outFile3 = new File(outFileName3);
        	
        	if(inputFile.exists()) inputFile.delete();
        	if(outFile1.exists()) outFile1.delete();
        	if(outFile2.exists()) outFile2.delete();
        	if(outFile3.exists()) outFile3.delete();
        }

    }
    
    
	   private static void removeExistingDocInfo(PdfReader reader, String outFileName) throws Exception{
	        try {
				PRStream stream = (PRStream)reader.getCatalog().getAsStream(com.lowagie.text.pdf.PdfName.METADATA);
				byte[] xmpBytes1 = PdfReader.getStreamBytes(stream);
				
				Document xmlDoc = byteToDOM(xmpBytes1);
				NodeList creatorList = xmlDoc.getElementsByTagName("dc:creator");
				for (int i = 0; i < creatorList.getLength(); i++) {
					Node childNode = creatorList.item(i);
					childNode.getParentNode().removeChild(childNode);
				}
				NodeList keywordsListDc = xmlDoc.getElementsByTagName("dc:subject");
				for (int i = 0; i < keywordsListDc.getLength(); i++) {
					Node childNode = keywordsListDc.item(i);
					childNode.getParentNode().removeChild(childNode);
				}
				NodeList keywordsList = xmlDoc.getElementsByTagName("pdf:Keywords");
				for (int i = 0; i < keywordsList.getLength(); i++) {
					Node childNode = keywordsList.item(i);
					childNode.getParentNode().removeChild(childNode);
				}

				PdfStamper stamper5 = new PdfStamper(reader, new FileOutputStream(outFileName));
				stamper5.setXmpMetadata(DOMtoByteArr(xmlDoc));
				stamper5.close();
				
			} catch (Exception e) {
				System.out.println("Exception in removeExistingDocInfo::"+e.getMessage());
				PdfStamper stamper5 = new PdfStamper(reader, new FileOutputStream(outFileName));
				//stamper5.setXmpMetadata(DOMtoByteArr(xmlDoc));
				stamper5.close();
				//e.printStackTrace();
			}
	    }
	   
		private static void closeObjects(PdfWriter writer, PdfReader reader, PdfReader reader1, PdfReader reader2, PdfReader reader3, com.itextpdf.kernel.pdf.PdfReader reader7, PdfDocument outputPDF, 
				ByteArrayOutputStream baos, ByteArrayOutputStream os, FileOutputStream fos, FileOutputStream fos2, PdfStamper stamper, PdfStamper stamper1, XmpWriter xmp, XmpWriter xmp1) {
			if(reader != null) reader.close();
			if(reader1 != null) reader1.close();
			if(reader2 != null) reader2.close();
			if(reader3 != null) reader3.close();
			try {
				if(reader7 != null) reader7.close();
				if(fos != null) fos.close();
				if(fos2 != null) fos2.close();
				if(baos != null) baos.close();
				if(os != null) os.close();
				if(stamper != null) stamper.close();
				if(stamper1 != null) stamper1.close();
				if(writer != null) writer.close();
				if(outputPDF != null) outputPDF.close();
				if(xmp != null) xmp.close();
				if(xmp1 != null) xmp1.close();
			} catch (Exception e) {
				System.out.println("Error closing stream:: "+e.getMessage());
				
			}
			
		}

	
	private static long hoursDifference(Date date1, Date date2) {
		final int MILLI_TO_HOUR = 1000 * 60 * 60;
		return (long) (date1.getTime() - date2.getTime()) / MILLI_TO_HOUR;
	}
	
    
	
	private static Document byteToDOM(byte[] documentoXml) throws Exception {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    return builder.parse(new ByteArrayInputStream(documentoXml));
	}
	
	private static byte[] DOMtoByteArr(Document doc) throws Exception{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		 ByteArrayOutputStream bos=new ByteArrayOutputStream();
		 StreamResult result=new StreamResult(bos);
		 transformer.transform(source, result);
		 byte []array=bos.toByteArray();
		 
		 return array;
	}
	
	private static boolean isNullOrBlank(String str){
		if (str == null || str.trim().equalsIgnoreCase("")) return true;
		else return false;
	}
	
	/**
	 * *.pdf file name filter
	 */
	class pdfFilenameFilter implements FilenameFilter {

		public boolean accept(File file, String name) {			
			return (name.toLowerCase().endsWith(".pdf") );
		}
	}
	

}
