package com.daniele.fantalive.bl;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.checkerframework.framework.qual.InvisibleQualifier;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotSession;

import com.daniele.fantalive.bl.Main.Campionati;
import com.daniele.fantalive.model.Return;
import com.daniele.fantalive.model.Squadra;
import com.daniele.fantalive.util.Constant;

public class FantaCronacaLiveBOT extends TelegramLongPollingBot{

	private static BotSession registerBot;
	private static String CHI;
	

	@Override
	public void onUpdateReceived(Update update) {
	}

	@Override
	public String getBotUsername() {
		return "FantaCronacaLiveBot";
	}

	@Override
	public String getBotToken() {
		return Constant.TOKEN_BOT_FANTACRONACALIVE;
	}

	public static void main(String[] args) throws Exception {
		Constant c=null;
		Class<?> cl = Class.forName("fantalive.util.ConstantDev");
		Method method = cl.getDeclaredMethod("constant");
		c = (Constant) method.invoke(c);		
		FantaCronacaLiveBOT bot = inizializza("MAIN");
		bot.inviaMessaggio(c.CHAT_ID_FANTALIVE, "ciao");
	}

	public static FantaCronacaLiveBOT inizializza(String chi) throws Exception {
		ApiContextInitializer.init();
		TelegramBotsApi api = new TelegramBotsApi();
		FantaCronacaLiveBOT f = new FantaCronacaLiveBOT();
		try {
			registerBot = api.registerBot(f);
		} catch (TelegramApiRequestException e) {
			throw new RuntimeException(e);
		}
		f.messaggioBenvenuto();
		return f;
	}
	public void inviaMessaggio(long chatId,String msg) throws TelegramApiException {
		try {
			execute(creaSendMessage(chatId, msg ));
		} catch (TelegramApiException e) {
			throw e;
		}
	}
	private void messaggioBenvenuto() throws Exception {
		inviaMessaggio(Constant.CHAT_ID_FANTALIVE, Constant.CIAO + " Benvenuto!" +
		Constant.PARTITA_FINITA +
		Constant.PARTITA_NON_FINITA  +
		Constant.OK_VOTO +
		Constant.NO_VOTO_IN_CORSO +
		Constant.PALLONE +
		Constant.SVEGLIA  +
		Constant.OROLOGIO +
		Constant.SCHEDULATA +
		Constant.DEFINITIVA +
		Constant.NO_VOTO_FINITO  +
		Constant.RIGORE_PARATO +
		Constant.NO_VOTO_DA_INIZIARE +
		Constant.SCHIERATO +
		Constant.NON_SCHIERATO +
		Constant.SEMAFORO_1 +
		Constant.SEMAFORO_2 +
		Constant.IMBATTUTO +
		Constant.ASSIST +	
		Constant.GOL +
		Constant.USCITO +
		Constant.ENTRATO +
		Constant.GOL_ANNULLATO +
		Constant.INFORTUNIO +
		Constant.AMMONITO +
		Constant.ESPULSO +
		Constant.GOL_SUBITO +
		Constant.RIGORE_SBAGLIATO +  
		Constant.RIGORE_SEGNATO +
		Constant.AUTOGOL +	
		Constant.CIAO +
		Constant.KEEP_ALIVE );

		
	}
	private SendMessage creaSendMessage(long chatId,String msg) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.setChatId(chatId);
		String messaggio="";
		messaggio = messaggio + "\n" + msg;
		sendMessage.setText(messaggio);
		return sendMessage;
	}

	public void startBot() {
		registerBot.start();
	}
	public void stopBot() {
		registerBot.stop();
	}
	public boolean isRunning() {
		return registerBot.isRunning();
	}

}
