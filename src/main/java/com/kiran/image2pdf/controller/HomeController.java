package com.kiran.image2pdf.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.kiran.image2pdf.model.ImageConfig;
import com.kiran.image2pdf.model.beans.DownloadBean;
import com.kiran.image2pdf.service.PdfService;
import com.kiran.image2pdf.utils.templates.config.TemplateConfig;
import com.kiran.image2pdf.utils.templates.config.TemplateConfig.Template;
import com.kiran.image2pdf.utils.templates.config.TemplateConfig.Templates;
import com.kiran.image2pdf.utils.templates.pagesize.PageSize;
import com.kiran.image2pdf.utils.templates.pagesize.PageSize.Size;
import com.kiran.image2pdf.utils.templates.pagesize.PageSize.Sizes;

@Controller
public class HomeController extends BaseController {

	@Autowired
	PdfService pdfService;

	@Value("${config.template.path}")
	private String templateConfigLoaction;

	@Value("${config.page.path}")
	private String sizeConfigLocation;

	static List<Template> templateList;

	static List<Size> sizeList;

	@PostConstruct
	public void initTemplate() {

		try {

			File file = new File(templateConfigLoaction);
			JAXBContext jContext = JAXBContext.newInstance(TemplateConfig.class);
			Unmarshaller unmarshaller = jContext.createUnmarshaller();
			TemplateConfig templateConfig = (TemplateConfig) unmarshaller.unmarshal(file);
			Templates templates = templateConfig.getTemplates();
			templateList = templates.getTemplateList();

			File file2 = new File(sizeConfigLocation);
			JAXBContext jContext2 = JAXBContext.newInstance(PageSize.class);
			Unmarshaller unmarshaller2 = jContext2.createUnmarshaller();
			PageSize pageSize = (PageSize) unmarshaller2.unmarshal(file2);
			Sizes sizes = pageSize.getSizes();
			sizeList = sizes.getSizeList();

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = { "/", "/login" }, method = RequestMethod.GET)
	public ModelAndView login() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("pages/home");
		return modelAndView;
	}

	@RequestMapping(value = { "/home" }, method = RequestMethod.GET)
	public ModelAndView home() {

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("pages/home");

		return modelAndView;
	}

	@RequestMapping(value = { "/create" }, method = RequestMethod.GET)
	public ModelAndView create() {

		ModelAndView modelAndView = new ModelAndView();

		modelAndView.addObject("templateList", templateList);
		modelAndView.addObject("sizeList", sizeList);

		modelAndView.setViewName("pages/image-select");
		return modelAndView;
	}

	@PostMapping("/download")
	public ResponseEntity<InputStreamResource> download(@ModelAttribute DownloadBean downloadBean) {
		
		ModelAndView modelAndView = new ModelAndView();
		try {

			List<InputStream> inputPdfList = new ArrayList<>();
			File mergedFile = File.createTempFile("merged", ".pdf");
			OutputStream outputStream = new FileOutputStream(mergedFile);

			int templateImageCount = 0;
			for (Template tmpl : templateList) {
				if (String.valueOf(tmpl.getId()).equals(downloadBean.getTemplateId())) {
					templateImageCount = Integer.valueOf(tmpl.getImageCount());
				}
			}
			
			
			int selectedImageCount = downloadBean.getUploadImgs().length;
			//System.out.println("selectedImageCount: " + selectedImageCount);
			
			int pageCount = 0;

			pageCount = selectedImageCount / templateImageCount;
			//System.out.println("templateImageCount: " + templateImageCount);
			//System.out.println("pageCount: " + pageCount);
			
			
			if (selectedImageCount % templateImageCount > 0)
				pageCount++;

			// For each page start
			for (int i = 0; i < pageCount; i++) {
				
				//System.out.println("Inside page loop");
				int currentPageImageStartIndex = i * (templateImageCount - 1) + i;
				int currentPageImageEndIndex = (i + 1) * templateImageCount - 1;

				//System.out.println("currentPageImageStartIndex: " + currentPageImageStartIndex);
				//System.out.println("currentPageImageEndIndex: " + currentPageImageEndIndex);
				
				String[] imageList = new String[templateImageCount];
				String[] imageTitleList = new String[templateImageCount];
				
				int currentPageImageListIndex = 0;
				for (int j = currentPageImageStartIndex; j <= currentPageImageEndIndex; j++) {
					
					if( j < downloadBean.getUploadImgs().length)
					{
						imageList[currentPageImageListIndex] = Base64.encodeBase64String(downloadBean.getUploadImgs()[j].getBytes());
						imageTitleList[currentPageImageListIndex] = downloadBean.getImageTitles()[j];
					}
					currentPageImageListIndex++;
				}

				ImageConfig imageConfig = new ImageConfig();
				imageConfig.setImageList(imageList);
				imageConfig.setImageTitles(imageTitleList);
				
				for (Template tmpl : templateList) {
					if (String.valueOf(tmpl.getId()).equals(downloadBean.getTemplateId())) {
						imageConfig.setTemplate(tmpl);
					}
				}

				for (Size sz : sizeList) {
					if (String.valueOf(sz.getId()).equals(downloadBean.getSizeId())) {
						imageConfig.setSize(sz);
					}
				}

				try {
					
					File pdfFile = File.createTempFile(LocalDateTime.now().toString().replace(" ", ""), ".pdf");
					createPdf(pdfFile.getName(), imageConfig, downloadBean.getPageTitle());
					InputStream fileInputStream = new FileInputStream(pdfFile.getName());
					inputPdfList.add(fileInputStream);
					
					pdfFile.delete();
					
				} catch (IOException e) {
					e.printStackTrace();
					modelAndView.addObject("message", "error");
				} catch (Exception ee) {
					ee.printStackTrace();
					modelAndView.addObject("message", "error");
				}

			}
			// For each page end

			mergePdfFiles(inputPdfList,outputStream);
			// Below part will be create for each individual pages

			File fileToDownload = mergedFile;

			InputStreamResource resource = new InputStreamResource(new FileInputStream(fileToDownload));
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			String dateStr = formatter.format(now);
			
			
			try {
				
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=myPDF-" + dateStr + ".pdf")
						.contentType(MediaType.APPLICATION_PDF).contentLength(fileToDownload.length()).body(resource);
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			finally {
				mergedFile.delete();
			}
			

		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}

	public void mergePdfFiles(List<InputStream> inputPdfList, OutputStream outputStream) throws Exception {

		// Create document and pdfReader objects.
		com.itextpdf.text.Document document = new com.itextpdf.text.Document();
		List<com.itextpdf.text.pdf.PdfReader> readers = new ArrayList<com.itextpdf.text.pdf.PdfReader>();
		int totalPages = 0;

		// Create pdf Iterator object using inputPdfList.
		Iterator<InputStream> pdfIterator = inputPdfList.iterator();

		// Create reader list for the input pdf files.
		while (pdfIterator.hasNext()) {
			InputStream pdf = pdfIterator.next();
			com.itextpdf.text.pdf.PdfReader pdfReader = new com.itextpdf.text.pdf.PdfReader(pdf);
			readers.add(pdfReader);
			totalPages = totalPages + pdfReader.getNumberOfPages();
		}

		// Create writer for the outputStream
		com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);

		// Open document.
		document.open();

		// Contain the pdf data.
		PdfContentByte pageContentByte = writer.getDirectContent();

		PdfImportedPage pdfImportedPage;
		int currentPdfReaderPage = 1;
		Iterator<com.itextpdf.text.pdf.PdfReader> iteratorPDFReader = readers.iterator();

		// Iterate and process the reader list.
		while (iteratorPDFReader.hasNext()) {
			com.itextpdf.text.pdf.PdfReader pdfReader = iteratorPDFReader.next();
			// Create page and add content.
			while (currentPdfReaderPage <= pdfReader.getNumberOfPages()) {
				document.newPage();
				pdfImportedPage = writer.getImportedPage(pdfReader, currentPdfReaderPage);
				pageContentByte.addTemplate(pdfImportedPage, 0, 0);
				currentPdfReaderPage++;
			}
			currentPdfReaderPage = 1;
		}

		// Close document and outputStream.
		outputStream.flush();
		document.close();
		outputStream.close();

		System.out.println("Pdf files merged successfully.");
	}

	public void createPdf(String dest, ImageConfig imageConfig,  String title) throws IOException {

		System.out.println("inside createPdf");
		String[] imageList = imageConfig.getImageList();
		String[] imageTitleList = imageConfig.getImageTitles();
		
		int imageCount = imageList.length;
		String titleDivHeightInMM = "7";
		String titleImageDivHeightInMM = "5";
		
		String rowCount = imageConfig.getTemplate().getRow();
		String colCount = imageConfig.getTemplate().getCol();

		String pageTypeName = imageConfig.getSize().getName();

		String pageHeightInMM = imageConfig.getSize().getyLength();
		String pageWidthInMM = imageConfig.getSize().getxLenght();

		String containerWidthInMM = String.valueOf((Integer.parseInt(pageWidthInMM) - 1));
		String containerHeightInMM;
		
		if(StringUtils.isNotBlank(title))
			containerHeightInMM = String.valueOf((Integer.parseInt(pageHeightInMM)  - Integer.parseInt(titleDivHeightInMM) - 1));
		else
			containerHeightInMM = String.valueOf((Integer.parseInt(pageHeightInMM) - 2));
		
		
		

		String rowHeightInPercent = String.valueOf((100 / Integer.parseInt(rowCount)));
		String rowWidthInPercent = "100";

		String colWidthInPercent = String.valueOf((100 / Integer.parseInt(colCount) - 2));
		String colheightInPercent = "95";

		float imageContainerHeightInMMFloat = (Float.parseFloat(rowHeightInPercent) * (95.0F / 100.0F))
				* Float.parseFloat(containerHeightInMM) / 100.0F;
		String imageContainerHeightInMM = String.valueOf((int) imageContainerHeightInMMFloat);
		
		float imageHeightInMMFloat = imageContainerHeightInMMFloat;
		String imageHeightInMM = String.valueOf((int) imageHeightInMMFloat);
		
		float imageWithTitleHeightInMMFloat = imageContainerHeightInMMFloat - Float.parseFloat(titleImageDivHeightInMM);
		String imageWithTitleHeightInMM = String.valueOf((int) imageWithTitleHeightInMMFloat);
		
		String html = "";
		String head = "";
		String body = "";
		String style = "";

		if(StringUtils.isNotBlank(body))
			System.out.println("Body is not black");
		
		
		style = "<style>@page { size: " + pageTypeName + "; margin: 0;}@media print {  html, body {  width: "
				+ pageWidthInMM + "mm;height: " + pageHeightInMM + "mm; }}" + ".container {width: " + containerWidthInMM
				+ "mm;height: " + containerHeightInMM + "mm;padding: 3px 1px 1px 1px;}" + ".row {height:" + rowHeightInPercent
				+ "%;width:" + rowWidthInPercent + "%;padding:1px 3px 1px 3px;}" + ".column {float: left;width: "
				+ colWidthInPercent + "%;padding: 5px;height: " + colheightInPercent + "%;}"
				+ ".imageContainer {height:" + imageContainerHeightInMM + "mm;}"
				+ ".image { width: 100%;height: " + imageHeightInMM + "mm;margin: auto;object-fit: contain;display:block;}"
				+ ".imageWithTitle { width: 100%;height: " + imageWithTitleHeightInMM + "mm;margin: auto;object-fit: contain;display:block;}"
				+ ".page-title {height:" + titleDivHeightInMM + "mm;text-align: center;padding-top:3px;}"
				+ ".image-title {width: 100%;height: " + titleImageDivHeightInMM + "mm;text-align: center;}"
				+ "</style>";
		
		head = "<head><title>Home</title>" + style + "</head>";
		
		String titleDiv = "";
		if(StringUtils.isNotBlank(title))
		{
			titleDiv  = "<div class='page-title' ><b>" + title + "</b></div>";
		}
		
		int rowCountInt = Integer.parseInt(rowCount);
		int colCountInt = Integer.parseInt(colCount);

		String colGridNum = String.valueOf(12 / Integer.parseInt(colCount));
		String boostrapColClass = "col-md-" + colGridNum;

		String rowImageString = "";
		int imageIndex = 0;

		for (int i = 0; i < rowCountInt || i < imageList.length; i++) {
			String colImageString = "";
			for (int j = 0; j < colCountInt; j++) {
				if (imageCount > 0 && imageList[imageIndex] != null) {
					
					String imageClassName = "";
					String imageTitleDiv = "";
					if(imageTitleList[imageIndex] != null)
					{
						imageClassName = "imageWithTitle";
						imageTitleDiv = "<div class='image-title' ><small>" + imageTitleList[imageIndex] + "</small></div>";
					}
					else
						imageClassName = "image";
					
					
					colImageString += "<div class='column' >" + "<div class='" + boostrapColClass + " imageContainer'>"
							+ imageTitleDiv
							+ "<img class='" + imageClassName + "' src='data:image/jpg;base64," + imageList[imageIndex]
							+ "'></div></div>";
				}

				imageCount--;
				imageIndex++;
			}
			rowImageString += "<div class='row' >" + colImageString + "</div>";
		}

		body = "<body>" + titleDiv + "<div class='container'>" + rowImageString + "</div></body>";

		html = "<html>" + head + body + "</html>";

		ConverterProperties properties = new ConverterProperties();
		// properties.setBaseUri(baseUri);
		PdfWriter writer = new PdfWriter(dest);
		PdfDocument pdf = new PdfDocument(writer);

		Document document = HtmlConverter.convertToDocument(html, pdf, properties);

		document.close();
	}

}
