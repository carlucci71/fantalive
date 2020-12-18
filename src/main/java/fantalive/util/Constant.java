package fantalive.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Constant {
	public static Integer GIORNATA;
	public static Long CHAT_ID_FANTALIVE;
	public static String SPONTIT_KEY;
	public static String TOKEN_BOT_FANTALIVE;
	public static String SPONTIT_USERID;
	public static String APPKEY_FG;
	public static String AUTH_FS;
	public static boolean LIVE_FROM_FILE;
	public static boolean DISABILITA_NOTIFICA_TELEGRAM;
	public static ZonedDateTime KEEP_ALIVE_END;
	public static ZonedDateTime LAST_KEEP_ALIVE;
	public static int SCHEDULED_SNAP;
	/*-------------------------------------------------------------------------------------------*/
	public static DateTimeFormatter dateTimeFormatterIn = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneId.of("Europe/Rome"));
	public static DateTimeFormatter dateTimeFormatterOut = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("Europe/Rome"));
	public final static String BUSTA = "\u2709";
	public final static String CLESSIDRA  = "\u23F3";
	public final static String SPUNTA = "\u2714";
	public final static String INTERROGATIVO = "\u2753";
	public final static String PALLONE = "\u26BD";
	public final static String SVEGLIA  = "\u23F0";
	public final static String X_VERDE  = "\u274E";
	public final static String DIVANO = "\uD83D\uDECB";//55357 + 57035 
	public final static String PROVA = "\uD83D\uDC8B";
	
	
	public static void main(String[] args) {
		String str=DIVANO;
		for(int i=0;i<str.length();i++) {
			System.out.println("***");
//			System.out.println(Integer.toHexString(str.charAt(i)));
			System.out.println(Integer.toString(str.charAt(i)));
		}
	}
}
