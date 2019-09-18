package com.kiran.image2pdf.model;

import java.util.Arrays;

import com.kiran.image2pdf.utils.templates.config.TemplateConfig.Template;
import com.kiran.image2pdf.utils.templates.pagesize.PageSize.Size;

public class ImageConfig {
	
	private Template template;
	private Size size;
	private String[] imageList;
	
	public Template getTemplate() {
		return template;
	}
	public void setTemplate(Template template) {
		this.template = template;
	}
	public Size getSize() {
		return size;
	}
	public void setSize(Size size) {
		this.size = size;
	}
	public String[] getImageList() {
		return imageList;
	}
	public void setImageList(String[] imageList) {
		this.imageList = imageList;
	}
	@Override
	public String toString() {
		return "ImageConfig [template=" + template + ", size=" + size + ", imageList=" + Arrays.toString(imageList)
				+ "]";
	}
	
	
}
