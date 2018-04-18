package com.tiho.base.base.md;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Handler {
	
	private MessageDigest digest;
	
	public Md5Handler (){
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {e.printStackTrace();	}
	}
	
	public String md5Calc(File f){
		int i;
		String md5hash = null;		
		byte[] buffer = new byte[1024];
		int read = 0;
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}		
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			md5hash = bigInt.toString(16);
		}catch(Exception e) {
            e.printStackTrace();
        }finally{
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
		
		if(md5hash!=null&&md5hash.length() != 33){
			String tmp = "";
			for(i=1; i< (33-md5hash.length()); i++){
				tmp = tmp.concat("0");
			}
			md5hash = tmp.concat(md5hash);
		}
		
		return md5hash;
	}

}
