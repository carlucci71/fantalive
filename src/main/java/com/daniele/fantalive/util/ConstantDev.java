package com.daniele.fantalive.util;

import java.time.ZonedDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("DEV")
public class ConstantDev {
	
	@Bean
	public static  Constant constant() {
		Constant constant = new Constant();
		Constant.APPKEY_FG="4ab27d6de1e92c810c6d4efc8607065a735b917f";
		Constant.AUTH_FS="8966B6104A111A03719383281BBB7B3E2B67687D890F5BEC7D2E1C1517A0F5924C2745A44A5D4584B1440AFC4A7D57F0287E87C595DE5034F0AED342305F954F6F2661177C07966D13A5F0DCA02C4320A0E7EC7FF50B7807B498A7F22D2EB4A4F2185238ABDBDDE2B29E0009E2A7D0EE0E01C4BB";
		Constant.CHAT_ID_FANTALIVE=425497266l;
		Constant.DISABILITA_NOTIFICA_TELEGRAM=false;
		Constant.GIORNATA=20;
		Constant.KEEP_ALIVE_END=ZonedDateTime.now().plusHours(1);
		Constant.LIVE_FROM_FILE=true;
		Constant.TOKEN_BOT_FANTALIVE="1363620575:AAFLWIL9BMb6Lvu5giG-xJwbm7hyTKCHnhY";
		Constant.TOKEN_BOT_FANTACRONACALIVE="1333528917:AAEACBx5Eo1LPrpCQw_O20RiQTSDATl_Ty4";
		Constant.SCHEDULED_SNAP=20000;
		Constant.NUM_SQUADRE_BE=8;
		return constant;
	}
}
