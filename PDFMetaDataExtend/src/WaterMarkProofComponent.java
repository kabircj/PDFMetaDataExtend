import java.io.File;
import java.io.FileOutputStream;

import com.itextpdf.io.font.constants.StandardFontFamilies;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;


public class WaterMarkProofComponent {
	
	// have c:\temp\watermark directory created
	// have the sample.pdf under that dird
	private static final File SOURCE_PDF = new File("sample.pdf"); 
	private static final File RESULT_PDF = new File("result.pdf");
	private static final File TEMP_PDF = new File("temp.pdf"); 

	public static void main(String[] args) throws Exception {		
		watermarkPDF();		
		addHeaderFooter();
		if(TEMP_PDF.exists()) {
			TEMP_PDF.delete();
		}
	}
	
	public static void watermarkPDF() throws Exception {
		try {
			String filePath = SOURCE_PDF.getAbsolutePath();
			PdfReader reader = new PdfReader(filePath);
	        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(TEMP_PDF));
	       
	        Font f = new Font(FontFamily.HELVETICA, 128);
	        f.setColor(BaseColor.LIGHT_GRAY);
	        Phrase p = new Phrase("ABCD Proof",f);

	        Rectangle pagesize;
	        float x, y;
			int num = reader.getNumberOfPages();

	        for (int i = 1; i <= num; i++) {
	        	pagesize = reader.getPageSizeWithRotation(i);
	            x = (pagesize.getLeft() + pagesize.getRight()) / 2;
	            y = (pagesize.getTop() + pagesize.getBottom()) / 2;
	            
	        	PdfContentByte under = stamper.getUnderContent(i);
	        	under.saveState();
	        	PdfGState gs1 = new PdfGState();
	        	gs1.setFillOpacity(0.5f);
	        	under.setGState(gs1);
	        	ColumnText.showTextAligned(under, Element.ALIGN_CENTER, p, x,y, 45f);
	        	under.restoreState();
	        }
	        stamper.close();
	        reader.close();

		}catch(Exception e) {
			throw e;
		}
	}
	
	public static void addHeaderFooter() throws Exception {
		try {
			String [] paragraphs = null;			
			PdfDocument pdfDoc = new PdfDocument(new com.itextpdf.kernel.pdf.PdfReader(TEMP_PDF), new com.itextpdf.kernel.pdf.PdfWriter(RESULT_PDF));
			com.itextpdf.kernel.geom.Rectangle pageSize;
			PdfCanvas pdfcanvas;
			int n = pdfDoc.getNumberOfPages();
			int heightCord = 15;
			int widthCord = 190;
			
			for (int i = 1; i <= n; i++) {
				PdfPage page = pdfDoc.getPage(i);
				pageSize = page.getPageSize();
				pdfcanvas = new PdfCanvas(page);
				pdfcanvas.beginText().setFontAndSize(
						PdfFontFactory.createFont(StandardFontFamilies.HELVETICA), 6)
		    			.beginText()
		    			.moveText(pageSize.getWidth()/2-190 , pageSize.getHeight()-5 )
		    			.showText("This is the author's version of an article that has been published in ABCD. Changes were made to this version by the publisher prior to publication.")
		    			.endText()
		    			.beginText()
		    			.moveText(pageSize.getWidth()/2-100 , pageSize.getHeight()-15 )
		    			.showText("The final version of record is available at	http://xx.abc.org/")
		    			.endText();
		    			if(paragraphs != null) {
		    				for(String para:paragraphs) {
		    					pdfcanvas.beginText().setFontAndSize(
			    				PdfFontFactory.createFont(StandardFontFamilies.HELVETICA), 6)
		    					.beginText()
				    			.moveText(pageSize.getWidth() / 2 - widthCord,heightCord)
				    			.showText(para)
				    			.endText();
		    					 widthCord -=90;
		    					 heightCord -= 10;
		    				}
		    			}else {
	    					pdfcanvas.beginText().setFontAndSize(
	    					PdfFontFactory.createFont(StandardFontFamilies.HELVETICA), 6)
			    			.beginText()
			    			.moveText(pageSize.getWidth() / 2 - 190, 15)
			    			.showText("sample statement")
			    			.endText();
		    			}
		    			
			}     
			pdfDoc.close();
		}catch(Exception e) {
			throw e;
		}
	}
}
