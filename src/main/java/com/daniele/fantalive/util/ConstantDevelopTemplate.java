package com.daniele.fantalive.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("DEVTEMPLATE")
public class ConstantDevelopTemplate {

	@Bean
	public static Constant constant() throws Exception {
		Constant constant=new Constant();
		constant.instanza="";
		constant.ABILITA_REFRESH=true;
		constant.DISABILITA_NOTIFICA_TELEGRAM=true;
//		constant.GIORNATA=12;
//		constant.KEEP_ALIVE_END="202110312300";
		constant.LIVE_FROM_FILE=false;
		constant.NUM_SQUADRE_BE=8;
//		constant.GIORNATA_FORZATA=3;
		constant.RITARDO=20;
		constant.SCHEDULED_SNAP=20000;
		constant.disableCertificateValidation=true;
		return constant;
	}

}
