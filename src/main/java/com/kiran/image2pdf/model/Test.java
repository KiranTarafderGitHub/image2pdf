package com.kiran.image2pdf.model;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String height = "297";
		String rowCount = "2";
		
		String rowHeightInPercent = String.valueOf((100/Integer.parseInt(rowCount)));
		
		float gg = 94.0F/100.0F;
		float percentOfRowHeight = Float.parseFloat(rowHeightInPercent)*(94.0F/100.0F);
		
		float imageContainerHeightInMMFloat = (Float.parseFloat(rowHeightInPercent)*(94.0F/100.0F))*Float.parseFloat(height)/100.0F;
		System.out.println("imageContainerHeightInMMFloat" +imageContainerHeightInMMFloat);
		
		int imageContainerHeightInMMInt = (int)imageContainerHeightInMMFloat;
		System.out.println("imageContainerHeightInMMInt" +imageContainerHeightInMMInt);
		
		
		String imageContainerHeightInMM = String.valueOf((int)imageContainerHeightInMMFloat);
		
		
		System.out.println("rowHeightInPercent:" +rowHeightInPercent);

		System.out.println("percentOfRowHeight:" +percentOfRowHeight);
		
		System.out.println("imageContainerHeightInMM:" +imageContainerHeightInMM);
 
		
		

	}

}
