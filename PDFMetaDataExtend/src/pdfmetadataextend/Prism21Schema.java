package pdfmetadataextend;

import com.lowagie.text.xml.xmp.XmpSchema;

public class Prism21Schema extends XmpSchema { 
  
 public static final String DEFAULT_XPATH_ID = "prism"; 
 public static final String DEFAULT_XPATH_URI  
    = "http://prismstandard.org/namespaces/basic/3.0/"; 
  
 public static final String PUBLICATIONNAME 		= "prism:publicationName";
 public static final String VOLUME 				= "prism:volume";
 public static final String ISSUE_IDENTIFIER 	= "prism:issueIdentifier"; 
 public static final String NUMBER 				= "prism:number";
 public static final String DOI 				= "prism:doi"; 
 public static final String STARTING_PAGE 		= "prism:startingPage";
 public static final String ENDING_PAGE 		= "prism:endingPage"; 
 public static final String COVER_DATE 			= "prism:coverDate"; 
 public static final String COVER_DISPLAY_DATE 	= "prism:coverDisplayDate"; 
 
   
 public Prism21Schema() { 
  super("xmlns:"  
                + DEFAULT_XPATH_ID  
                + "=\"" + DEFAULT_XPATH_URI + "\""); 
 } 
 
}
