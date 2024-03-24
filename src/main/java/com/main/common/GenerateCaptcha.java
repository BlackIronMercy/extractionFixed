package com.main.common;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.noise.CurvedLineNoiseProducer;
import cn.apiclub.captcha.text.producer.DefaultTextProducer;
import cn.apiclub.captcha.text.renderer.DefaultWordRenderer;

@Component
public class GenerateCaptcha {
	@Autowired
	AESEncryption aesEncryption;
	private static final Logger log = LogManager.getLogger(GenerateCaptcha.class);
	Random random = new Random();
	
	public DataContainer doGetCaptcha() {
		DataContainer data = new DataContainer();
		try {
			
			  Captcha captcha1 = createCaptcha(150, 40);
				String captchaimg =     encodeCaptcha(captcha1);
				String captcha = captcha1.getAnswer();
				String captchaEnc = aesEncryption.encrypt(captcha);
		    data.setMsg(GlobalConstant.MSG_SUCCESS);
		    data.setCaptcha(captchaEnc);
		    data.setData(captchaimg);
			
		} catch (Exception e) {
			log.error(e);
			data.setMsg(GlobalConstant.MSG_ERROR);
		}
		    return data;
		  }
		  
		  public static Captcha createCaptcha(Integer width, Integer height) {
				
				return new Captcha.Builder(width, height)
						.addBackground(new GradiatedBackgroundProducer())
						.addText(new DefaultTextProducer(), new DefaultWordRenderer())
						.addNoise(new CurvedLineNoiseProducer())
						.build();
			}
			
			//Converting to binary String
			public static String encodeCaptcha(Captcha captcha) {
				String image = null;
				try {
					ByteArrayOutputStream bos= new ByteArrayOutputStream();
					ImageIO.write(captcha.getImage(),"jpg", bos);
					byte[] byteArray= Base64.getEncoder().encode(bos.toByteArray());
					image = new String(byteArray);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return image;
			}
			
			
	}

