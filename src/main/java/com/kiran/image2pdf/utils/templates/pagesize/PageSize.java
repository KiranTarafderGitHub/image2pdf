package com.kiran.image2pdf.utils.templates.pagesize;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "pagesize-config")
public class PageSize {

	@XmlElement(name = "sizes")
	private Sizes sizes;

	public Sizes getSizes() {
		return sizes;
	}

	public static class Sizes {
		@XmlElement(name = "size")
		private List<Size> sizeList = new ArrayList<PageSize.Size>();

		public List<Size> getSizeList() {
			return sizeList;
		}

	}

	public static class Size {
		@XmlAttribute(name = "id")
		private int id;
		@XmlAttribute(name = "name")
		private String name;

		@XmlAttribute(name = "x-lenght")
		private String xLenght;

		@XmlAttribute(name = "y-lenght")
		private String yLength;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getxLenght() {
			return xLenght;
		}

		public String getyLength() {
			return yLength;
		}

	}
}
