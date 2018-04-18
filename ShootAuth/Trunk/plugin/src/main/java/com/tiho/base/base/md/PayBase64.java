package com.tiho.base.base.md;
/*
* @(#)SkyBase64.java 1.0 2012-12-29
*
* Copyright (c) 1998-2012 PowerPlay
* All rights reserved.
*
*/

/**
* 该base64为变种base64，为我们自己sky自有的base64加密，变种表生成工具用C写成，如需要
* 替换变种表，请找相应的人员。
* @version 1.0 2012-12-29
* @author seward.huang
*/

public class PayBase64 {
	
	/***********************通用的表*****************************/
	
//	 static final char intToBase64[] = { 'A', 'B', 'C', 'D', 'E', 'F', /* 索引 0 ~ 5*/
//         'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',  /* 索引6 ~ 18*/
//         'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',  /* 索引 19 ~ 31*/
//         'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',  /* 索引 32 ~ 44*/
//         't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',  /* 索引 45 ~ 57*/
//         '6', '7', '8', '9', '+', '/' };  /* 索引58 ~ 63*/
//	 
//	 static final byte base64ToInt[] = { -1, -1, -1, -1, -1, -1, -1, -1,
//         -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
//         -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
//         -1, 62/* 符号+*/, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1,
//         -1, -1, -1, -1, -1, -1, 0 /* 符号A */, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
//         13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1,
//         -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
//         41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };

	 /***********************变种的表*****************************/
	 
	 static final char intToBase64[] = { 
		 '1','h','f','k','P','V','R','D','W','r','E','w','n','Q','4','v',
		 '8','9','G','K','F','t','c','7','S','J','q','5','b','Z','T','6',
		 'M','u','Y','j','A','o','i','_','2','N','d','0','y','s','a','X',
		 'I','m','-','H','l','e','g','x','U','B','C','O','L','z','3','p'
		 };  /* 索引58 ~ 63*/
	 
	 static final byte base64ToInt[] = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,50,-1,-1,
		 43,0,40,62,14,27,31,23,16,17,-1,-1,-1,-1,-1,-1,
		 -1,36,57,58,7,10,20,18,51,48,25,19,60,32,41,59,
		 4,13,6,24,30,56,5,8,47,34,29,-1,-1,-1,-1,39,
		 -1,46,28,22,42,53,2,54,1,38,35,3,52,49,12,37,
		 63,26,9,45,21,33,15,11,55,44,61};
	 
	 
	 public static String encode(byte[] a) {
	     int aLen = a.length; //总长度
	     int numFullGroups = aLen / 3; //以3个byte组成以4个字符为一组的组数
	     int numBytesInPartialGroup = aLen - 3 * numFullGroups; //余数
	     int resultLen = 4 * ((aLen + 2) / 3); //输出长度总是4倍数，如果有余数，(aLen+2)/3保证将余数包含，并有空间放置填充符=
	     StringBuffer result = new StringBuffer(resultLen);
	  
	     int inCursor = 0;
	     for (int i = 0; i < numFullGroups; i++) {
	         int byte0 = a[inCursor++] & 0xff;
	         int byte1 = a[inCursor++] & 0xff;
	         int byte2 = a[inCursor++] & 0xff;
	         result.append(intToBase64[byte0 >> 2]);
	         result.append(intToBase64[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
	         result.append(intToBase64[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
	         result.append(intToBase64[byte2 & 0x3f]);
	     }
//	     处理余数
	     if (numBytesInPartialGroup != 0) {
	         int byte0 = a[inCursor++] & 0xff;
	         result.append(intToBase64[byte0 >> 2]);
	         //余数为1
	         if (numBytesInPartialGroup == 1) {
	             result.append(intToBase64[(byte0 << 4) & 0x3f]);
	             result.append("==");
	         } else {
	             // 余数为2
	             int byte1 = a[inCursor++] & 0xff;
	             result.append(intToBase64[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
	             result.append(intToBase64[(byte1 << 2) & 0x3f]);
	             result.append('=');
	         }
	     }
	     
//	     if (numBytesInPartialGroup != 0){
//	    	 byte a1 = a[inCursor];
//	    	 byte b = (inCursor+1 < aLen) ? a[inCursor+1] : 0;
//				
//	    	 result.append(intToBase64[(a1 >> 2) & 0x3F]);
//	    	 result.append(intToBase64[(((a1 & 3) << 4) + (b >> 4)) & 0x3F]);
//	    	 result.append((inCursor+1 < aLen) ? intToBase64[(((b & 0xf) << 2)) & 0x3F] : '=');
//	    	 result.append('=');
//			}
			
			
	     return result.toString();
	 }
	 public static byte[] decode(String s) throws Exception {
	       //字符总长必须是4的倍数
	       int sLen = s.length();
	       int numGroups = sLen / 4;
	       if (4 * numGroups != sLen)
	           throw new IllegalArgumentException(
	                   "字串长度必须是4的倍数");
	       //余1个byte则算漏了两个byte，余2个byte则算漏掉了1个byte
	       int missingBytesInLastGroup = 0;
	       int numFullGroups = numGroups;
	       if (sLen != 0) {
	           //余2个byte的情况
	           if (s.charAt(sLen - 1) == '=') {
	               missingBytesInLastGroup++;
	               //如果有余数发生，则完整3个byte组数少一个。
	               numFullGroups--;
	           }
	           //余1个byte的情况
	           if (s.charAt(sLen - 2) == '=')
	               missingBytesInLastGroup++;
	       }
	       //总字节长度
	       byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];
	    
	       try {
	           int inCursor = 0, outCursor = 0;
	           for (int i = 0; i < numFullGroups; i++) {
	               int ch0 = base64toInt(s.charAt(inCursor++), base64ToInt);
	               int ch1 = base64toInt(s.charAt(inCursor++), base64ToInt);
	               int ch2 = base64toInt(s.charAt(inCursor++), base64ToInt);
	               int ch3 = base64toInt(s.charAt(inCursor++), base64ToInt);
	               result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
	               result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
	               result[outCursor++] = (byte) ((ch2 << 6) | ch3);
	           }
	           if (missingBytesInLastGroup != 0) { 
	               int ch0 = base64toInt(s.charAt(inCursor++), base64ToInt);
	               int ch1 = base64toInt(s.charAt(inCursor++), base64ToInt);
	               //不管余1还是余2个byte，肯定要解码一个byte。
	               result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
	    
	               //如果余2个，即差一个才构成3byte，那么还要解码第二个byte。
	               if (missingBytesInLastGroup == 1) {
	                   int ch2 = base64toInt(s.charAt(inCursor++), base64ToInt);
	                   result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
	               }
	           }
	       } catch (Exception e) {
	           throw e;
	       }
	       return result;
	   }
	    
	   private static int base64toInt(char c, byte[] alphaToInt) throws Exception {
	       int result = alphaToInt[c];
	       if (result < 0)
	           throw new Exception("err index value");
	       return result;
	   }
}
