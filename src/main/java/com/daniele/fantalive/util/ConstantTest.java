package com.daniele.fantalive.util;

import java.lang.reflect.Method;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class ConstantTest   {

	@SuppressWarnings("static-access")
	@Bean
	public static Constant constant() throws Exception {
		Constant constant=null;
		Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
		Method method = cl.getDeclaredMethod("constant");
		constant = (Constant) method.invoke(constant);		
		constant.instanza=ConstantTest.class.getCanonicalName();
		constant.LIVE_FROM_FILE=true;
		constant.PREPARA_SQUADRE_ON_LOAD=false;
		constant.DISABILITA_NOTIFICA_TELEGRAM=true;
		return constant;
	}

}
