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
		if(System.getenv("KEEP_ALIVE_END") != null) {
			constant.KEEP_ALIVE_END = ZonedDateTime.parse(System.getenv("KEEP_ALIVE_END"), Constant.dateTimeFormatterIn);
		}
		else {
			constant.KEEP_ALIVE_END = ZonedDateTime.now();
		}
		constant.DISABILITA_NOTIFICA_TELEGRAM = Boolean.valueOf(System.getenv("DISABILITA_NOTIFICA_TELEGRAM"));
		constant.LIVE_FROM_FILE = Boolean.valueOf(System.getenv("LIVE_FROM_FILE"));
		constant.ABILITA_REFRESH = Boolean.valueOf(System.getenv("ABILITA_REFRESH"));
		constant.CHAT_ID_FANTALIVE = Long.valueOf(System.getenv("CHAT_ID_FANTALIVE"));
		constant.TOKEN_BOT_FANTALIVE = System.getenv("TOKEN_BOT_FANTALIVE");
		constant.TOKEN_BOT_FANTACRONACALIVE = System.getenv("TOKEN_BOT_FANTACRONACALIVE");
		constant.TOKEN_BOT_RISULTATICONRITARDO = System.getenv("TOKEN_BOT_RISULTATICONRITARDO");
		constant.APPKEY_FG = System.getenv("APPKEY_FG");
		constant.GIORNATA = Integer.valueOf(System.getenv("GIORNATA"));
		constant.UTENTE_FS = System.getenv("UTENTE_FS");
		constant.PWD_FS = System.getenv("PWD_FS");
		constant.NUM_SQUADRE_BE = Integer.valueOf(System.getenv("NUM_SQUADRE_BE"));
		if (System.getenv("SCHEDULED_SNAP") != null) {
			constant.SCHEDULED_SNAP=Integer.valueOf(System.getenv("SCHEDULED_SNAP"));
		} else {
			constant.SCHEDULED_SNAP=20000;
		}
		return constant;
	}
}
