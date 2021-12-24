package com.daniele.fantalive.bl;

import java.lang.reflect.Method;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotSession;

import com.daniele.fantalive.util.Constant;

public class RisultatiConRitardoBOT extends TelegramLongPollingBot{

	private static BotSession registerBot;
	@Override
	public void onUpdateReceived(Update update) {
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
		ApiContextInitializer.init();
		TelegramBotsApi api = new TelegramBotsApi();
		RisultatiConRitardoBOT f = new RisultatiConRitardoBOT();
		try {
			registerBot = api.registerBot(f);
		} catch (TelegramApiRequestException e) {
			throw new RuntimeException(e);
		}
		return f;
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
