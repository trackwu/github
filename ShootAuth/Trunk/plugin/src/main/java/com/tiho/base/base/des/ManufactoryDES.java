package com.tiho.base.base.des;

public class ManufactoryDES {
	// String text = "&skytest&skym900";  
	// String result1 = DES.encryptDES(text,key);  
	// String result2 = DES.decryptDES(result1, key);
	
    public static String[] decode(String str){
		String key = "s5^c6a4x";
		String result="";
		try {
			result = DES.decryptDES(str, key);
		} catch (Exception e) {
			e.printStackTrace();
			result = "&android&playmet";
		}
		return result.split("\\&");
    }
    
	public static String encode(String manu, String type) {
		String key = "s5^c6a4x";
		String encryptString = "&" + manu + "&" + type;
		String result = "";
		try {
			while (encryptString.length() < 16) {
				encryptString = "0" + encryptString;
			}
			result = DES.encryptDES(encryptString, key);
		} catch (Exception e) {
			e.printStackTrace();
			result =  "1Jph/zsXM3hLBFd8XbBLM2ooLrUu+wvC";
		}
		return result;
	}
    
}