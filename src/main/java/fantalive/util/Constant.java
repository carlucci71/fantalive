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
	public static DateTimeFormatter dateTimeFormatterIn = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneId.of("Europe/Rome"));
	public static DateTimeFormatter dateTimeFormatterOut = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("Europe/Rome"));
	public static ZonedDateTime LAST_KEEP_ALIVE;
}
