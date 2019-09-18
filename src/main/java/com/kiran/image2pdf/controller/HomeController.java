package com.kiran.image2pdf.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.kiran.image2pdf.model.ImageConfig;
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
	private Environment env;

	@Autowired
	PdfService pdfService;

	private static List<Template> templateList;
	private static List<Size> sizeList;

	@PostConstruct
	public void initTemplate() {

		String templateConfigLoaction = env.getProperty("TemplateConfigFileLocation");
		String sizeConfigLocation = env.getProperty("PageSizeConfigFileLocation");

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
	public ResponseEntity<InputStreamResource> download(@RequestParam String templateId, @RequestParam String sizeId,
			@RequestParam String userImageCount, @RequestParam("uploadImgs") MultipartFile[] uploadImgs) {
		ModelAndView modelAndView = new ModelAndView();
		try {

			String[] imageList = new String[uploadImgs.length];
			ImageConfig imageConfig = new ImageConfig();

			for (Template tmpl : templateList) {
				if (String.valueOf(tmpl.getId()).equals(templateId)) {
					imageConfig.setTemplate(tmpl);
				}
			}

			for (Size sz : sizeList) {
				if (String.valueOf(sz.getId()).equals(sizeId)) {
					imageConfig.setSize(sz);
				}
			}

			int index = 0;
			for (MultipartFile imgFile : uploadImgs) {
				imageList[index] = Base64.encodeBase64String(imgFile.getBytes());
				index++;
			}
			imageConfig.setImageList(imageList);
			
			TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
			df.setTimeZone(tz);
			String nowDate = df.format(new Date());

			
			
			String DEST = "images-to-pdf.pdf";

			try {

				createPdf(DEST, imageConfig);

			} catch (IOException e) {
				e.printStackTrace();
				modelAndView.addObject("message", "error");
			} catch (Exception ee) {
				ee.printStackTrace();
				modelAndView.addObject("message", "error");
			}

			File fileToDownload = new File(DEST);

			InputStreamResource resource = new InputStreamResource(new FileInputStream(fileToDownload));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=myPDF-" + nowDate )
					.contentType(MediaType.APPLICATION_PDF).contentLength(fileToDownload.length()).body(resource);

		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}

	public void createPdf(String dest, ImageConfig imageConfig) throws IOException {

		String[] imageList = imageConfig.getImageList();
		int imageCount = imageList.length;

		String rowCount = imageConfig.getTemplate().getRow();
		String colCount = imageConfig.getTemplate().getCol();

		String pageTypeName = imageConfig.getSize().getName();

		String pageHeightInMM = imageConfig.getSize().getyLength();
		String pageWidthInMM = imageConfig.getSize().getxLenght();

		String containerWidthInMM = String.valueOf((Integer.parseInt(pageWidthInMM) - 2));
		String containerHeightInMM = String.valueOf((Integer.parseInt(pageHeightInMM) - 2));

		String rowHeightInPercent = String.valueOf((100 / Integer.parseInt(rowCount)));
		String rowWidthInPercent = "100";

		String colWidthInPercent = String.valueOf((100 / Integer.parseInt(colCount) - 2));
		String colheightInPercent = "95";

		float imageContainerHeightInMMFloat = (Float.parseFloat(rowHeightInPercent) * (94.0F / 100.0F))
				* Float.parseFloat(pageHeightInMM) / 100.0F;

		String imageContainerHeightInMM = String.valueOf((int) imageContainerHeightInMMFloat);

		String html = "";
		String head = "";
		String body = "";
		String style = "";

		style = "<style>@page { size: " + pageTypeName + "; margin: 0;}@media print {  html, body {  width: "
				+ pageWidthInMM + "mm;height: " + pageHeightInMM + "mm; }}" + ".container {width: " + containerWidthInMM
				+ "mm;height: " + containerHeightInMM + "mm;padding: 2px;}" + ".row {height:" + rowHeightInPercent
				+ "%;width:" + rowWidthInPercent + "%;padding:3px;}" + ".column {float: left;width: "
				+ colWidthInPercent + "%;padding: 5px;height: " + colheightInPercent + "%;}"
				+ ".imageContainer {height:" + imageContainerHeightInMM + "mm;}"
				+ ".imageContainer img{ width: 100%;height: " + imageContainerHeightInMM
				+ "mm;margin: auto;object-fit: contain;display:block;}</style>";
		head = "<head><title>Home</title>" + style + "</head>";

		int rowCountInt = Integer.parseInt(rowCount);
		int colCountInt = Integer.parseInt(colCount);

		String colGridNum = String.valueOf(12 / Integer.parseInt(colCount));
		String boostrapColClass = "col-md-" + colGridNum;

		String rowImageString = "";
		int imageIndex = 0;

		for (int i = 0; i < rowCountInt || i < imageList.length; i++) {
			String colImageString = "";
			for (int j = 0; j < colCountInt; j++) {
				if (imageCount > 0) {
					colImageString += "<div class='column' >" + "<div class='" + boostrapColClass + " imageContainer'>"
							+ "<img class='image' src='data:image/jpg;base64," + imageList[imageIndex]
							+ "'></div></div>";
				}

				imageCount--;
				imageIndex++;
			}
			rowImageString += "<div class='row' >" + colImageString + "</div>";
		}

		body = "<body><div class='container'>" + rowImageString + "</div></body>";

		html = "<html>" + head + body + "</html>";

		ConverterProperties properties = new ConverterProperties();
		//properties.setBaseUri(baseUri);
		PdfWriter writer = new PdfWriter(dest);
		PdfDocument pdf = new PdfDocument(writer);

		Document document = HtmlConverter.convertToDocument(html, pdf, properties);

		document.close();
	}

}
