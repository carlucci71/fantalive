package com.daniele.asta;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

@Component
public class Criptaggio {
	protected static String encrypt(String sPlainText, String x) throws Exception
	{
		if (sPlainText==null || sPlainText.trim().equals("")) return "";
		byte[] plainText = sPlainText.getBytes();
		byte[] key = "A1.xMCPP0x-!log?".getBytes(StandardCharsets.UTF_8);
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] doFinal = cipher.doFinal(plainText);
		String encodeBase64String = Base64.encodeBase64String(doFinal);
		return encodeBase64String;
	}
	/*
	protected static String decrypt(String cipherText, String x) throws Exception
	{
		byte[] key = "A1.xMCPP0x-!log?".getBytes(StandardCharsets.UTF_8);
		byte[] decode = Base64.decodeBase64(cipherText);
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] doFinal = cipher.doFinal(decode);
		return new String(doFinal);
	}
	*/

}