package com.daniele.fantalive.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Constant {
	public static Integer GIORNATA;
	public static Long CHAT_ID_FANTALIVE;
	public static String TOKEN_BOT_FANTALIVE;
	public static String TOKEN_BOT_FANTACRONACALIVE;
	public static String APPKEY_FG;
	public static String AUTH_FS;
	public static boolean LIVE_FROM_FILE;
	public static boolean DISABILITA_NOTIFICA_TELEGRAM;
	public static ZonedDateTime KEEP_ALIVE_END;
	public static ZonedDateTime LAST_KEEP_ALIVE;
	public static int SCHEDULED_SNAP;
	public static  int NUM_SQUADRE_BE = 0;	
	public static final String FORMAZIONE = "formazione";
	public static ZonedDateTime LAST_REFRESH;
	/*-------------------------------------------------------------------------------------------*/
	public static DateTimeFormatter dateTimeFormatterIn = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneId.of("Europe/Rome"));
	public static DateTimeFormatter dateTimeFormatterOut = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("Europe/Rome"));
	public final static String PARTITA_FINITA = "\u2B2C";
	public final static String PARTITA_NON_FINITA  = "\u23F3";
	public final static String OK_VOTO = "\u2714";
	public final static String NO_VOTO_IN_CORSO = "\uFE16";
	public final static String PALLONE = "\u26BD";
	public final static String SVEGLIA  = "\u23F0";
	public final static String OROLOGIO = "\u23F0";
	public final static String SCHEDULATA = "\uD83D\uDD5B";
	public final static String DEFINITIVA = " \uD83D\uDEE0";
	public final static String NO_VOTO_FINITO  = "\uD83C\uDCA2";
	public static final String RIGORE_PARATO = "\uD83D\uDD95";
	public final static String NO_VOTO_DA_INIZIARE = "\uD83D\uDECF";//"\uD83D\uDECB";
	public final static String SCHIERATO = "\uD83D\uDCAA";
	public final static String NON_SCHIERATO = "\uD83D\uDE4F";
	public final static String SEMAFORO_1 = "\uD83D\uDEA6";
	public final static String SEMAFORO_2 = "\uD83D\uDEA5";
	public static final String IMBATTUTO = "\uD83E\uDD45";
	public static final String ASSIST = "\uD83D\uDEB8";	
	public static final String GOL = "\u26BD";//\u26F3";//"\uD83E\uDD42";
	public static final String USCITO =  "\u2B62";
	public static final String ENTRATO =  "\u2B60";
	public static final String GOL_ANNULLATO = "\uD83D\uDEC2";
	public static final String INFORTUNIO = "\uD83D\uDE91";
	public static final String AMMONITO = "\uD83D\uDFE8";
	public static final String ESPULSO = "\uD83D\uDFE5";
	public static final String GOL_SUBITO =  "\uD83C\uDF50";
	public static final String RIGORE_SBAGLIATO =   "\uD83D\uDE40";  
	public static final String RIGORE_SEGNATO = "\uD83E\uDD4A";
	public static final String AUTOGOL = "\uD83D\uDCA9";	
	public static final String CIAO = "\uD83D\uDC4B";
	public static final String KEEP_ALIVE = "\uD83E\uDE7A";
	public static final String P = "\u24C5";	
	public static final String D = "\u24B9";	
	public static final String C = "\u24B8";	
	public static final String A = "\u24B6";
	public static final String T1 = "\u2160";
	public static final String T2 = "\u2161";
	public static final String T3 = "\u2162";
	public static final String T4 = "\u2163";
	public static final String T5 = "\u2164";
	public static final String T6 = "\u2165";
	public static final String T7 = "\u2166";
	public static final String T8 = "\u2167";
	public static final String T9 = "\u2168";
	public static final String T10 = "\u2169";
	public static final String T11 = "\u2169" + "\u2160";
	public static final String R1 = "\u2776";
	public static final String R2 = "\u2777";
	public static final String R3 = "\u2778";
	public static final String R4 = "\u2779";
	public static final String R5 = "\u277A";
	public static final String R6 = "\u277B";
	public static final String R7 = "\u277C";
	public static final String R8 = "\u277D";
	public static final String R9 = "\u277E";
	public static final String R10 = "\u277F";
	public static final String R11 = "\u24EB";
	public static final String R12 = "\u24EC";
	public static final String R13 = "\u24ED";
	public static final String R14 = "\u24EE";
	public static final String R15 = "\u24EF";
	public static final String R16 = "\u24FA";
	public static final String R17 = "\u24FB";
	public static final String R18 = "\u24FC";
	public static final String R19 = "\u24FD";
	public static final String R20 = "\u24FE";
	public static final String PAUSA = "\uD83D\uDD14";
	public static final String FINE_PARTITA = "\uD83C\uDFC1";
}
