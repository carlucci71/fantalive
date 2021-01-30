package com.daniele.fantalive.bl;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
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

public class FantaLiveBOT extends TelegramLongPollingBot{

	private static BotSession registerBot;
	private static String CHI;
	

	@Override
	public void onUpdateReceived(Update update) {
		try {
			if(update.hasMessage()){
				Long chatId = update.getMessage().getChatId();
				String text = update.getMessage().getText();
				if(update.getMessage().hasText()){
					if(text.equals("/campionati")){
						execute(sendInlineKeyBoardCampionati(chatId,"campionati ",false,text));
					}
					else if(text.equals("/proiezioni")){
						execute(sendInlineKeyBoardCampionati(chatId,"proiezioni ",true,text));
					}
					else if(text.equals("/simulazioni")){
						execute(sendInlineKeyBoardPartiteSimulate(chatId,"simula ",text));
					}
					else {
						execute(creaSendMessage(chatId,text, true));//REPLY
					}
				}
			}
			else if(update.hasCallbackQuery()){
				final AnswerCallbackQuery answer = new AnswerCallbackQuery();
				answer.setShowAlert(false);
				answer.setCallbackQueryId(update.getCallbackQuery().getId());
				answer.setText("OK: " + update.getCallbackQuery().getData());
				Long chatId = update.getCallbackQuery().getMessage().getChatId();
				String testoCallback = update.getCallbackQuery().getData();
				if (testoCallback.startsWith("campionati")) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					execute(sendInlineKeyBoardSquadre(chatId,testoCallback, null,testoCallback));
				}
				else if (testoCallback.startsWith("proiezioni")) {
					String campionato =testoCallback.substring(testoCallback.indexOf(" ")+1);
					Map<String, Object> proiezioni = Main.proiezioni(campionato);
					execute(sendInlineKeyBoardSquadre(chatId,campionato, (List<String>) proiezioni.get("squadre"),(String) proiezioni.get("testo")));
				}
				else if (testoCallback.startsWith("dettaglio")) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					String[] split = testoCallback.split("#");
					execute(creaSendMessage(chatId,Main.getDettaglio(chatId,split[0],split[1]), false));

				}
				else if (testoCallback.startsWith("simulata")) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					Map<String, Object> proiezioni = Main.getPartitaSimulata(chatId,testoCallback);
					execute(sendInlineKeyBoardSquadre(chatId,(String) proiezioni.get("campionato"), (List<String>) proiezioni.get("squadre"),(String) proiezioni.get("testo")));

				}
				else {
					execute(creaSendMessage(chatId, "cosa hai mandato? " + testoCallback , false));
					answer.setShowAlert(true);
				}
				execute(answer);
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
		return "FantaLiveBot";
	}

	@Override
	public String getBotToken() {
		return Constant.TOKEN_BOT_FANTALIVE;
	}

	public static void main(String[] args) throws Exception {
		Constant c=null;
		
		Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
		Method method = cl.getDeclaredMethod("constant");
		c = (Constant) method.invoke(c);		
		
		Main.init(null,null,c);
		inizializza("MAIN");
	}

	public static FantaLiveBOT inizializza(String chi) throws Exception {
		ApiContextInitializer.init();
		TelegramBotsApi api = new TelegramBotsApi();
		FantaLiveBOT f = new FantaLiveBOT();
		try {
			registerBot = api.registerBot(f);
		} catch (TelegramApiRequestException e) {
			throw new RuntimeException(e);
		}
		CHI=chi;
		Main.MIO_IP = InetAddress.getLocalHost().getHostAddress();
		return f;
	}

	public void inviaMessaggio(long chatId,String msg,boolean bReply) throws TelegramApiException {
		try {
			execute(creaSendMessage(chatId, msg , bReply));
		} catch (TelegramApiException e) {
			throw e;
		}
	}
	private SendMessage sendInlineKeyBoardPartiteSimulate(long chatId, String cb, String testo){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoPartiteSimulate());
		return new SendMessage().enableHtml(true).setParseMode("html").setChatId(chatId).setText(testo).setReplyMarkup(inlineKeyboardMarkup);
	}
	private SendMessage sendInlineKeyBoardCampionati(long chatId, String testoCallBack, boolean all, String testo){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoCampionati(testoCallBack,all));
		return new SendMessage().enableHtml(true).setParseMode("html").setChatId(chatId).setText(testo).setReplyMarkup(inlineKeyboardMarkup);
	}
	private SendMessage sendInlineKeyBoardSquadre(long chatId, String campionato, List<String> squadrePuntuali, String testo){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoSquadre(campionato,squadrePuntuali));
		return new SendMessage().enableHtml(true).setParseMode("html").setChatId(chatId).setText(testo).setReplyMarkup(inlineKeyboardMarkup);
	}
	private List<List<InlineKeyboardButton>> generaElencoSquadre(String campionato, List<String> squadrePuntuali) {
		try {
			List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
			Map<String, Return> go = Main.go(false, null, null);
			Set<String> campionati=new HashSet<>();
			if (campionato.equalsIgnoreCase("ALL")) {
				campionati=go.keySet();
			} else {
				campionati.add(campionato);
			}
			for (String attCamp : campionati) {
				Return return1 = go.get(attCamp);
				List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
				List<Squadra> squadre = return1.getSquadre();
				for (Squadra squadra : squadre) {
					if (squadrePuntuali == null || squadrePuntuali.contains(attCamp + "-" + squadra.getNome())) {
						if (keyboardButtonsRow1.size()>2) {
							rowList.add(keyboardButtonsRow1);
							keyboardButtonsRow1 = new ArrayList<>();
						}
						keyboardButtonsRow1.add(new InlineKeyboardButton().setText(squadra.getNome()).setCallbackData("dettaglio " + attCamp + "#"+ squadra.getNome()));
					}
				}
				rowList.add(keyboardButtonsRow1);
			}
			return rowList;
		}
		catch (Exception e ) {
			throw new RuntimeException(e);
		}
	}
	private List<List<InlineKeyboardButton>> generaElencoPartiteSimulate() {
		try {
			List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
			Campionati[] campionati = Main.Campionati.values();
			for (Campionati campionato : campionati) {
				List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
				Set<String> getpartiteSimulate = Main.getpartiteSimulate(campionato.name());
				for (String partitaSimulata : getpartiteSimulate) {
					keyboardButtonsRow1.add(new InlineKeyboardButton().setText(partitaSimulata).setCallbackData("simulata " + partitaSimulata));
				}
				rowList.add(keyboardButtonsRow1);
			}
			return rowList;
		}
		catch (Exception e ) {
			throw new RuntimeException(e);
		}
	}
	private List<List<InlineKeyboardButton>> generaElencoCampionati(String cb, boolean all) {
		try {
			List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
			Campionati[] campionati = Main.Campionati.values();
			List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
			if (all) {
				keyboardButtonsRow1.add(new InlineKeyboardButton().setText("ALL").setCallbackData(cb + "ALL"));
			}
			for (Campionati campionato : campionati) {
				keyboardButtonsRow1.add(new InlineKeyboardButton().setText(campionato.name()).setCallbackData(cb + campionato.name()));
			}
			rowList.add(keyboardButtonsRow1);
			return rowList;
		}
		catch (Exception e ) {
			throw new RuntimeException(e);
		}
	}
	private SendMessage creaSendMessage(long chatId,String msg, boolean bReply) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
//		AGGIUNGE TASTIERA		
//		sendMessage.enableMarkdown(true);
//		setButtons(sendMessage);
//		RIMUOVE TASTIERA		
//		ReplyKeyboardRemove replyKeyboardMarkup = new ReplyKeyboardRemove();
//		sendMessage.setReplyMarkup(replyKeyboardMarkup);

		sendMessage.setChatId(chatId);
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
		}
		
		sendMessage.setText(messaggio);
		return sendMessage;
	}

	private synchronized void setButtons(SendMessage sendMessage) {
		// Create a keyboard
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		sendMessage.setReplyMarkup(replyKeyboardMarkup);
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboard(false);

		// Create a list of keyboard rows
		List<KeyboardRow> keyboard = new ArrayList<>();

		// First keyboard row
		KeyboardRow keyboardFirstRow = new KeyboardRow();
		// Add buttons to the first keyboard row
		keyboardFirstRow.add(new KeyboardButton("Hi"));

		// Second keyboard row
		KeyboardRow keyboardSecondRow = new KeyboardRow();
		// Add the buttons to the second keyboard row
		keyboardSecondRow.add(new KeyboardButton("Help"));

		// Add all of the keyboard rows to the list
		keyboard.add(keyboardFirstRow);
		keyboard.add(keyboardSecondRow);
		// and assign this list to our keyboard
		replyKeyboardMarkup.setKeyboard(keyboard);
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
