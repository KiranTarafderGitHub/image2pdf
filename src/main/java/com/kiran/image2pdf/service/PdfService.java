package com.kiran.image2pdf.service;

import java.net.MalformedURLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.VerticalAlignment;


@Service
public class PdfService {
	
	
	@Autowired
	private Environment	env;
	
	
	public PdfService()
	{
		
	}
	
	
	public String buildPDF(String dest,String imgcount,String row,String col,String orientation,List<String> images) {
		
		try
		{
			//File file = new File(dest);
	        //file.getParentFile().mkdirs();
	        
	        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
	        
	        Document doc = new Document(pdfDoc, new PageSize(PageSize.A4).rotate());
	        
	        
	        doc = attachImages(doc, imgcount, row, col, orientation, images);
	        
	        
	        /*Float docWidth = doc.getStrokeWidth();
	        Float docHeight = doc.getHeight();
	        ImageData imgData = ImageDataFactory.create(imgFileDir+images.get(0));
	        Image img = new Image(imgData);
	        doc.add(img);*/
	        
	        doc.close();
	        
	        return "success";
	        /*
	         * 
	         *  Image img = new Image(ImageDataFactory.create(MARY), 320, 750, 50);
				document.add(img);
	         */
	        
	        
	        

		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "error";
		}
	}
	
	public Document attachImages(Document document,String imgcount,String row,String col,String orientation,List<String> images)
	{
		int rowInt = Integer.valueOf(row);
		int colInt = Integer.valueOf(col);
		
		int heightPercent = 0;
		int widthPercent = 0;
		
		if(rowInt <= 1)
			heightPercent = 85;
		else if(rowInt == 2)
			heightPercent = 40;
		else
			heightPercent = 29;
		
		
		if(colInt <= 1)
			widthPercent = 95;
		else if(colInt == 2)
			widthPercent = 45;
		else
			widthPercent = 30;
		
		
		System.out.println("heightPercent: "+heightPercent+" ---- widthPercent: "+widthPercent);
		
		String image_dir_windows = env.getProperty("image_dir_windows");
        String imgFileDir = "file:///"+image_dir_windows;
 
        
        Table table = new Table(colInt);
        table.setWidthPercent(100F);
		
		try {
			
			float width= PageSize.A4.getWidth()*((float)widthPercent/100);
			float height = PageSize.A4.getHeight()*((float)heightPercent/100) - document.getTopMargin() - document.getBottomMargin();
			//float height = PageSize.A4.getHeight()*((float)heightPercent/100);
			
			System.out.println("height: "+height+" ---- width: "+width);
			
			for(String imagePath: images)
			{
				Rectangle rectangle = new Rectangle(width, height);
				
				
				String path = imgFileDir+imagePath;
				ImageData imgData = ImageDataFactory.create(imgFileDir+imagePath);
		        Image img = new Image(imgData);
		        
		        //img.scaleToFit(width, height);
		        img.scaleAbsolute(width, height);
		        //img.scale(width, height);
		        img.setHeight(height);
		        
		        
		        
		        Cell cell = new Cell();
		        cell.add(img);
		        
		        cell.setHorizontalAlignment(HorizontalAlignment.CENTER);
		        //cell.setHeight(height);
		        //cell.setBorder(Border.NO_BORDER);
				
				table.addCell(cell);
			}
			
			table.setVerticalAlignment(VerticalAlignment.MIDDLE);
			document.add(table);
			return document;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return null;
	}
}
