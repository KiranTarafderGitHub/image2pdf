package com.kiran.image2pdf.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

	@GetMapping(value = { "/", "/login" })
	public ModelAndView login() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("pages/home");
		return modelAndView;
	}

	@GetMapping(value = { "/home" })
	public ModelAndView home() {

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("pages/home");

		return modelAndView;
	}

	@GetMapping(value = { "/create" })
	public ModelAndView create() {

		ModelAndView modelAndView = new ModelAndView();

		modelAndView.addObject("templateList", templateList);
		modelAndView.addObject("sizeList", sizeList);

		modelAndView.setViewName("pages/image-select");
		return modelAndView;
	}

	@PostMapping("/download")
	public ResponseEntity<InputStreamResource> download(@ModelAttribute DownloadBean downloadBean) {

		try {

			List<String> htmlPageList = generateHtmlPages(downloadBean);

			// Create a PDF document
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			PdfWriter writer = new PdfWriter(byteArrayOutputStream);
			PdfDocument pdf = new PdfDocument(writer);
			ConverterProperties properties = new ConverterProperties();

			Document document = null;
			String completeHtml = "";

			if (CollectionUtils.isNotEmpty(htmlPageList)) {
				for (String htmlPage : htmlPageList) {
					completeHtml += htmlPage;
				}
			}

			document = HtmlConverter.convertToDocument(completeHtml, pdf, properties);

			// Close the PDF document
			document.close();
			pdf.close();

			// Set up HTTP headers for the response
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_PDF);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			String downloadName;
			if (StringUtils.isNoneBlank(downloadBean.getPageTitle())) {
				downloadName = downloadBean.getPageTitle();
			} else {
				downloadName = "myPDF-" + formatter.format(LocalDateTime.now());
			}

			headers.setContentDispositionFormData("attachment", downloadName + ".pdf");

			// Create an InputStreamResource from the generated PDF content
			InputStreamResource resource = new InputStreamResource(
					new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

			// Return the PDF as a ResponseEntity
			return ResponseEntity.ok().headers(headers).body(resource);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	public List<String> generateHtmlPages(DownloadBean downloadBean) {
		
		List<String> htmlPageList = new ArrayList<>();
		try {

			int templateImageCount = 0;
			for (Template tmpl : templateList) {
				if (String.valueOf(tmpl.getId()).equals(downloadBean.getTemplateId())) {
					templateImageCount = Integer.valueOf(tmpl.getImageCount());
				}
			}

			int selectedImageCount = downloadBean.getUploadImgs().length;
			// System.out.println("selectedImageCount: " + selectedImageCount);

			int pageCount = 0;

			if (selectedImageCount % templateImageCount > 0)
				pageCount = (selectedImageCount / templateImageCount) + 1;
			else
				pageCount = selectedImageCount / templateImageCount;

			System.out.println("templateImageCount: " + templateImageCount);
			System.out.println("pageCount: " + pageCount);

			// For each page start
			for (int i = 0; i < pageCount; i++) {

				System.out.println("Inside page loop");
				int currentPageImageStartIndex = i * (templateImageCount - 1) + i;
				int currentPageImageEndIndex = (i + 1) * templateImageCount - 1;

				System.out.println("currentPageImageStartIndex: " + currentPageImageStartIndex);
				System.out.println("currentPageImageEndIndex: " + currentPageImageEndIndex);

				String[] imageList = new String[templateImageCount];
				String[] imageTitleList = new String[templateImageCount];

				int currentPageImageListIndex = 0;
				for (int j = currentPageImageStartIndex; j <= currentPageImageEndIndex; j++) {

					if (j < downloadBean.getUploadImgs().length) {
						imageList[currentPageImageListIndex] = Base64
								.encodeBase64String(downloadBean.getUploadImgs()[j].getBytes());
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

					String singleHtmlPageStr = createPdfHtml(imageConfig, downloadBean.getPageTitle());
					System.out.println("Successfully build single page");
					htmlPageList.add(singleHtmlPageStr);

				} catch (IOException e) {
					System.out.println(e.getMessage());
				} catch (Exception ee) {
					System.out.println(ee.getMessage());
				}

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return htmlPageList;
	}

	public String createPdfHtml(ImageConfig imageConfig, String title) throws IOException {

		System.out.println("inside createPdfHtml for a single page");
		String[] imageList = imageConfig.getImageList();
		String[] imageTitleList = imageConfig.getImageTitles();

		int imageCount = imageList.length;
		System.out.println("Total no of images for this page is " + imageCount);
		String titleDivHeightInMM = "7";
		String titleImageDivHeightInMM = "5";

		String rowCount = imageConfig.getTemplate().getRow();
		String colCount = imageConfig.getTemplate().getCol();

		String pageTypeName = imageConfig.getSize().getName();

		String pageHeightInMM = imageConfig.getSize().getyLength();
		String pageWidthInMM = imageConfig.getSize().getxLenght();

		String containerWidthInMM = String.valueOf((Integer.parseInt(pageWidthInMM) - 1));
		String containerHeightInMM;

		if (StringUtils.isNotBlank(title))
			containerHeightInMM = String
					.valueOf((Integer.parseInt(pageHeightInMM) - Integer.parseInt(titleDivHeightInMM) - 1));
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

		if (StringUtils.isNotBlank(body))
			System.out.println("Body is not black");

		style = "<style>@page { size: " + pageTypeName + "; margin: 0;}@media print {  html, body {  width: "
				+ pageWidthInMM + "mm;height: " + pageHeightInMM + "mm; }}" + ".container {width: " + containerWidthInMM
				+ "mm;height: " + containerHeightInMM + "mm;padding: 3px 1px 1px 1px;}" + ".row {height:"
				+ rowHeightInPercent + "%;width:" + rowWidthInPercent + "%;padding:1px 3px 1px 3px;}"
				+ ".column {float: left;width: " + colWidthInPercent + "%;padding: 5px;height: " + colheightInPercent
				+ "%;}" + ".imageContainer {height:" + imageContainerHeightInMM + "mm;}"
				+ ".image { width: 100%;height: " + imageHeightInMM
				+ "mm;margin: auto;object-fit: contain;display:block;}" + ".imageWithTitle { width: 100%;height: "
				+ imageWithTitleHeightInMM + "mm;margin: auto;object-fit: contain;display:block;}"
				+ ".page-title {height:" + titleDivHeightInMM + "mm;text-align: center;padding-top:3px;}"
				+ ".image-title {width: 100%;height: " + titleImageDivHeightInMM + "mm;text-align: center;}"
				+ "</style>";

		head = "<head><title>Home</title>" + style + "</head>";

		String titleDiv = "";
		if (StringUtils.isNotBlank(title)) {
			titleDiv = "<div class='page-title' ><b>" + title + "</b></div>";
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
					if (imageTitleList[imageIndex] != null) {
						imageClassName = "imageWithTitle";
						imageTitleDiv = "<div class='image-title' ><small>" + imageTitleList[imageIndex]
								+ "</small></div>";
					} else
						imageClassName = "image";

					colImageString += "<div class='column' >" + "<div class='" + boostrapColClass + " imageContainer'>"
							+ imageTitleDiv + "<img class='" + imageClassName + "' src='data:image/jpg;base64,"
							+ imageList[imageIndex] + "'></div></div>";
				}

				imageCount--;
				imageIndex++;
			}
			rowImageString += "<div class='row' >" + colImageString + "</div>";
		}

		body = "<body>" + titleDiv + "<div class='container'>" + rowImageString + "</div></body>";

		html = "<html>" + head + body + "</html>";

		return html;
	}

}
