package com.main.common;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AESEncryption {
	private static final String ALGORITHM = "AES";
	
	@Value("${aesKey}")
	public String aesKey;
	
	

	public String encrypt(String plainTexttString1){
		String encoded =null;
		try {
			String plainTexttString = plainTexttString1.replace("+", "%2B");
			byte[] plainText = plainTexttString.getBytes(StandardCharsets.UTF_8);
			Key secretKey = new SecretKeySpec(aesKey.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			encoded = Base64.getEncoder().encodeToString(cipher.doFinal(plainText));
			
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return encoded;
	}

	public String decrypt2(String plainTexttString) {
		String decrypted = null;
		try {
			byte[] decoded = Base64.getDecoder().decode(plainTexttString);
			Key secretKey = new SecretKeySpec(aesKey.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			decrypted= new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
			
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return decrypted;
	}

}
