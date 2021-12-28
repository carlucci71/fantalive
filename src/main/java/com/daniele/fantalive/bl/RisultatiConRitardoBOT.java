package com.daniele.fantalive.bl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.daniele.fantalive.util.Constant;

public class RisultatiConRitardoBOT extends TelegramLongPollingBot{

	private static BotSession registerBot;
	@Override
	public void onUpdateReceived(Update update) {
		try {
			if(update.hasMessage()){
				Long chatId = update.getMessage().getChatId();
				String text = update.getMessage().getText();
				if(update.getMessage().hasText()){
					if(text.equals("/risultati")){
						String testoCallback = Main.getOldSnapPartite();
						execute(creaSendMessage(chatId, testoCallback , false));
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

	private SendMessage creaSendMessage(long chatId,String msg, boolean bReply) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.setChatId(Long.toString(chatId));
		sendMessage.setText(msg);
		return sendMessage;
	}

	
	@Override
	public String getBotUsername() {
		return "RisultatiConRitardoBot";
	}

	@Override
	public String getBotToken() {
		return Constant.TOKEN_BOT_RISULTATICONRITARDO;
	}

	public static void main(String[] args) throws Exception {
		Constant c=null;
		Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
		Method method = cl.getDeclaredMethod("constant");
		c = (Constant) method.invoke(c);		
		RisultatiConRitardoBOT bot = inizializza();
		try {
			bot.inviaMessaggio(c.CHAT_ID_FANTALIVE, "Status:STARTED");
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		System.err.println("FINE");
	}

	public static RisultatiConRitardoBOT inizializza() throws Exception {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		RisultatiConRitardoBOT risultatiConRitardoBOT = new RisultatiConRitardoBOT();
		registerBot=telegramBotsApi.registerBot(risultatiConRitardoBOT);
		return risultatiConRitardoBOT;
	}
	public void inviaMessaggio(long chatId,String msg) throws TelegramApiException {
		try {
			execute(creaSendMessage(chatId, msg ));
		} catch (TelegramApiException e) {
			throw e;
		}
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
