package com.daniele.fantalive.util;

import java.time.ZonedDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("protetto")
public class ConstantProtetto {
	
	@Bean
	public static Constant constant() {
		Constant constant = new Constant();
		constant.instanza=ConstantProtetto.class.getCanonicalName();
		constant.DISABILITA_NOTIFICA_TELEGRAM = Boolean.valueOf(System.getenv("DISABILITA_NOTIFICA_TELEGRAM"));
		constant.disableCertificateValidation = Boolean.valueOf(System.getenv("DISABILITA_CERTIFICATE_VALIDATION"));

		constant.LIVE_FROM_FILE = Boolean.valueOf(System.getenv("LIVE_FROM_FILE"));
		constant.ABILITA_REFRESH = Boolean.valueOf(System.getenv("ABILITA_REFRESH"));
		constant.CHAT_ID_FANTALIVE = Long.valueOf(System.getenv("CHAT_ID_FANTALIVE"));
		constant.TOKEN_BOT_FANTALIVE = System.getenv("TOKEN_BOT_FANTALIVE");
		constant.TOKEN_BOT_LINKATTIVAZIONEFANTALIVE= System.getenv("TOKEN_BOT_LINKATTIVAZIONEFANTALIVE");
		constant.TOKEN_BOT_FANTACRONACALIVE = System.getenv("TOKEN_BOT_FANTACRONACALIVE");
		constant.TOKEN_BOT_RISULTATICONRITARDO = System.getenv("TOKEN_BOT_RISULTATICONRITARDO");
		constant.APPKEY_FG = System.getenv("APPKEY_FG");
		constant.APPKEY_FG_MOBILE = System.getenv("APPKEY_FG_MOBILE");
		constant.GIORNATA = 1;//Integer.valueOf(System.getenv("GIORNATA"));
		constant.UTENTE_FS = System.getenv("UTENTE_FS");
		constant.PWD_FS = System.getenv("PWD_FS");
		constant.UTENTE_FG = System.getenv("UTENTE_FG");
		constant.PWD_FG = System.getenv("PWD_FG");
		constant.NUM_SQUADRE_BE = Integer.valueOf(System.getenv("NUM_SQUADRE_BE"));
		if (System.getenv("RITARDO") != null) {
			constant.RITARDO = Integer.valueOf(System.getenv("RITARDO"));
		}
		if (System.getenv("GIORNATA_FORZATA") != null) {
			constant.GIORNATA_FORZATA = Integer.valueOf(System.getenv("GIORNATA_FORZATA"));
		}
		if (System.getenv("SCHEDULED_SNAP") != null) {
			constant.SCHEDULED_SNAP=Integer.valueOf(System.getenv("SCHEDULED_SNAP"));
		} else {
			constant.SCHEDULED_SNAP=20000;
		}
		return constant;
	}
}
