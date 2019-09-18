package com.kiran.image2pdf.utils.templates.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "template-config")
public class TemplateConfig {

	@XmlElement(name = "templates")
	private Templates templates;

	public Templates getTemplates() {
		return templates;
	}

	public static class Templates {
		@XmlElement(name = "template")
		private List<Template> templateList = new ArrayList<Template>();

		public List<Template> getTemplateList() {
			return templateList;
		}

	}

	public static class Template {
		/* template-name="2 Image Horizontal" image-count="2" row="2" col="1" */
		@XmlAttribute(name = "id")
		private int id;

		@XmlAttribute(name = "template-name")
		private String templateName;

		@XmlAttribute(name = "image-count")
		private String imageCount;

		@XmlAttribute(name = "row")
		private String row;

		@XmlAttribute(name = "col")
		private String col;

		@XmlAttribute(name = "orientation")
		private String orientation;

		public int getId() {
			return id;
		}

		public String getTemplateName() {
			return templateName;
		}

		public String getImageCount() {
			return imageCount;
		}

		public String getRow() {
			return row;
		}

		public String getCol() {
			return col;
		}

		public String getOrientation() {
			return orientation;
		}

	}
}
