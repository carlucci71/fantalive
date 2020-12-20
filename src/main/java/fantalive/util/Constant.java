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
	public final static String DIVANO = "\uD83D\uDECB";
	public final static String SCHIERATO = "\uD83D\uDCAA";
	public final static String NON_SCHIERATO = "\uD83D\uDE4F";
	public final static String SEMAFORO = "\uD83D\uDEA6";
	public static final String IMBATTUTO = "\uD83E\uDD45";
	public static final String ASSIST = "\uD83C\uDF6C";	
	public static final String GOL = "\uD83E\uDD42";
	public static final String USCITO =  "\uD83D\uDE34";
	public static final String ENTRATO =  "\uD83E\uDD2B";
	public static final String GOL_ANNULLATO = "\uD83D\uDC7D";
	public static final String INFORTUNIO = "\uD83D\uDE91";
	public static final String AMMONITO = "\uD83D\uDFE8";
	public static final String ESPULSO = "\uD83D\uDFE5";
	public static final String GOL_SUBITO =  "\uD83C\uDF50";
	public static final String RIGORE_PARATO = "\u26C4\uFE0F";
	public static final String RIGORE_SBAGLIATO =   "\uD83D\uDE40";  
	public static final String RIGORE_SEGNATO = "\uD83E\uDD4A";
	public static final String AUTOGOL = "\uD83D\uDCA9";	
	public static final String CIAO = "\uD83D\uDC4B";
	public static final String KEEP_ALIVE = "\uD83E\uDE7A";	
	public static ZonedDateTime LAST_REFRESH;
}
