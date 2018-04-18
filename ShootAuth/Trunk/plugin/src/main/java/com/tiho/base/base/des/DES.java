package com.tiho.base.base.des;
/*
* @(#)Des.java 1.0 2012-12-29
*
* Copyright (c) 1998-2012 PowerPlay
* All rights reserved.
*
*/
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
* des加密工具，des有两种加密模式，ecb和cbc，目前该类只开放了cbc的功能，ecb模式加密不开放
* 
* @note 必须注意，该类为保密文件，请不要随便丢给其他开发人员使用。
* @version 1.0 2012-12-29
* @author seward.huang
*/
public class DES {
	private static boolean isECB = false;
	// private static byte[] iv = { 1, 2, 3, 4, 5, 6, 7, 8 };
	//向量表 密钥为：k*[awa%r
	//private static byte[] iv = { 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31 };
	private static byte[] iv = { 0x6B, 0x2A, 0x5B, 0x61, 0x77, 0x61, 0x25, 0x72 };
	public static String encryptDES(String encryptString, String encryptKey) throws Exception {
		// IvParameterSpec zeroIv = new IvParameterSpec(new byte[8]);
		IvParameterSpec zeroIv = new IvParameterSpec(iv);
		SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
		if (isECB) {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
			return SkyBase64.encode(encryptedData);

		} else {
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
			byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
			return SkyBase64.encode(encryptedData);
		}

	}

	public static String decryptDES(String decryptString, String decryptKey) throws Exception {
		byte[] byteMi;
		byteMi = new SkyBase64().decode(decryptString);
		IvParameterSpec zeroIv = new IvParameterSpec(iv);
		// IvParameterSpec zeroIv = new IvParameterSpec(new byte[8]);
		SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
		if (isECB) {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte decryptedData[] = cipher.doFinal(byteMi);
			return new String(decryptedData);
		} else {
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
			byte decryptedData[] = cipher.doFinal(byteMi);
			return new String(decryptedData);
		}
	}
}