package com.daniele.fantalive.bl;

import java.lang.reflect.Method;
import java.net.InetAddress;

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
	private static FantaCronacaLiveBOT fantaCronacaLiveBOT;


	@Override
	public void onUpdateReceived(Update update) {
		try {
			if(update.hasMessage()){
				Long chatId = update.getMessage().getChatId();
				String text = update.getMessage().getText();
				if(update.getMessage().hasText()){
					if (text.equals("killMe")) {
						fantaCronacaLiveBOT.stopBot();
					}
					else {
						execute(creaSendMessage(chatId,text, true));
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			//			throw new RuntimeException(e);
		}
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
		FantaCronacaLiveBOT bot = inizializza("MAIN");
		try {
			bot.inviaMessaggio(c.CHAT_ID_FANTALIVE, "Status:STARTED");
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		System.err.println("FINE");
	}

	public static FantaCronacaLiveBOT inizializza(String chi) throws Exception {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		fantaCronacaLiveBOT = new FantaCronacaLiveBOT();
		registerBot=telegramBotsApi.registerBot(fantaCronacaLiveBOT);
		CHI=chi;
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
				Constant.CIAO );


	}
	private SendMessage creaSendMessage(long chatId,String msg) {
		return creaSendMessage(chatId, msg,false);
	}

	private SendMessage creaSendMessage(long chatId,String msg, boolean bReply) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.setChatId(Long.toString(chatId));
		String messaggio="";
		String rep=" ";
		if (bReply) {
			for(int i=0;i<msg.length();i++) {
				rep = rep + "\\u" + Integer.toHexString(msg.charAt(i)).toUpperCase();
			}
			rep=rep+" ";

			rep = rep + " --> ";
			byte[] bytes = msg.getBytes();
			for (int i = 0; i < bytes.length; i++) {
				rep = rep + bytes[i] + ",";
			}
			messaggio="<b>sono il bot reply</b> per  " + chatId;
		}
		messaggio = messaggio + "\n" + msg;
		if(bReply) {
			messaggio = messaggio + "\n" + rep;
		}
		if (chatId == Constant.CHAT_ID_FANTALIVE) {
			messaggio = messaggio + "\n\n<i>" + CHI + " " + Main.MIO_IP + "</i>";
			messaggio = messaggio + "\n\n<i>" + Main.getUrlNotifica() + "</i>";
		}
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
