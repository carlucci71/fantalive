package com.daniele.fantalive.bl;

import java.lang.reflect.Method;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

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
		Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
		Method method = cl.getDeclaredMethod("constant");
		c = (Constant) method.invoke(c);		
		FantaCronacaLiveBOT bot = inizializza();
		try {
			bot.inviaMessaggio(c.CHAT_ID_FANTALIVE, "Status:STARTED");
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		System.err.println("FINE");
	}

	public static FantaCronacaLiveBOT inizializza() throws Exception {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		FantaCronacaLiveBOT fantaCronacaLiveBOT = new FantaCronacaLiveBOT();
		registerBot=telegramBotsApi.registerBot(fantaCronacaLiveBOT);
		return fantaCronacaLiveBOT;
	}
	public void inviaMessaggio(long chatId,String msg) throws TelegramApiException {
		try {
			while (msg.length()>4000) {
				execute(creaSendMessage(chatId, msg.substring(0,4000) ));
				msg=msg.substring(4000);
			}
			
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
		sendMessage.setChatId(Long.toString(chatId));
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
