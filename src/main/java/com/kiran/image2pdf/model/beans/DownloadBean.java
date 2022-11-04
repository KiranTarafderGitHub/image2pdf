package com.kiran.image2pdf.model.beans;

import java.util.Arrays;

import org.springframework.web.multipart.MultipartFile;

public class DownloadBean {

	String templateId;
	String sizeId;
	String userImageCount;
	MultipartFile[] uploadImgs;
	String pageTitle;
	String[] imageTitles;
	
	
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getSizeId() {
		return sizeId;
	}
	public void setSizeId(String sizeId) {
		this.sizeId = sizeId;
	}
	public String getUserImageCount() {
		return userImageCount;
	}
	public void setUserImageCount(String userImageCount) {
		this.userImageCount = userImageCount;
	}
	public MultipartFile[] getUploadImgs() {
		return uploadImgs;
	}
	public void setUploadImgs(MultipartFile[] uploadImgs) {
		this.uploadImgs = uploadImgs;
	}
	public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	
	public String[] getImageTitles() {
		return imageTitles;
	}
	public void setImageTitles(String[] imageTitles) {
		this.imageTitles = imageTitles;
	}
	@Override
	public String toString() {
		return "DownloadBean [templateId=" + templateId + ", sizeId=" + sizeId + ", userImageCount=" + userImageCount
				+ ", uploadImgs=" + Arrays.toString(uploadImgs) + ", pageTitle=" + pageTitle + ", imageTitles="
				+ Arrays.toString(imageTitles) + "]";
	}
}
