package com.daniele.fantalive.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class ConstantTest   {

	@SuppressWarnings("static-access")
	@Bean
	public static Constant constant() {
		Constant constant = ConstantDevelop.constant();
		constant.instanza=ConstantTest.class.getCanonicalName();
		constant.LIVE_FROM_FILE=true;
		constant.DISABILITA_NOTIFICA_TELEGRAM=true;
		return constant;
	}

}
