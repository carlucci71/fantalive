package com.daniele.fantalive.bl;

import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.daniele.fantalive.model.Live;
import com.daniele.fantalive.util.Constant;

public class RisultatiConRitardoBOT extends TelegramLongPollingBot{

	/*
risultati - risultati ultima giornata
live - risultati live ultima giornata
	 */
	private static BotSession registerBot;
	@Override
	public void onUpdateReceived(Update update) {
		try {
			if(update.hasMessage()){
				Long chatId = update.getMessage().getChatId();
				String text = update.getMessage().getText();
				if(update.getMessage().hasText()){
					if(text.equals("/risultati")){
						Map<String, Object> oldSnapPartite = Main.getOldSnapPartite(false);
						String testoCallback = (String) oldSnapPartite.get("testo");
						execute(creaSendMessage(chatId, testoCallback , false, (List<String>) oldSnapPartite.get("squadre")));
					}
					else if(text.equals("/live")){
						Map<String, Object> oldSnapPartite = Main.getOldSnapPartite(true);
						List<String> listSq = (List<String>) oldSnapPartite.get("squadre");
						if (listSq.size()>0) {
							String testoCallback = (String) oldSnapPartite.get("testo");
							execute(creaSendMessage(chatId, testoCallback , false, (List<String>) listSq));
						} else {
							execute(creaSendMessage(chatId, "nessuna partita live" , false, null));
						}
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
				if (testoCallback.startsWith("dettaglio ")) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					Map<String, Object> lives = Main.getLives(Constant.LIVE_FROM_FILE);
					List<Live> l = (List<Live>) lives.get("lives");
					for (Live live : l) {
						if (live.getSquadra().equalsIgnoreCase(testoCallback)) {
							String testo="";
							for (Map gioc : live.getGiocatori()) {
								testo=testo + gioc.get("nome") + " " + gioc.get("voto") + " ";
								String ev = (String) gioc.get("evento");
								String[] split = ev.split(",");
								for (String evento : split) {
									if (!evento.equals("")) {
										testo=testo + Main.desEvento(Integer.parseInt(evento), Constant.Campionati.BE.name()) + " ";
									}
								}
								testo = testo + "\n";
								//{nome=RUI PATRICIO, ruolo=P, voto=6.0, evento=4,4, id=4270}
								
								
							}
							if (testo.equals("")) {
								execute(creaSendMessage(chatId, "partita non ancora giocata", false, null));
							} else {
								execute(creaSendMessage(chatId, testo , false, null));
							}
						}
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

	private SendMessage creaSendMessage(long chatId,String msg, boolean bReply, List<String> elencoBottoni) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.setChatId(Long.toString(chatId));
		sendMessage.setText(msg);

		if (elencoBottoni != null) {
			InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
			List<List<InlineKeyboardButton>> listOfListButton = new ArrayList<>();
			int conta=0;
			List<InlineKeyboardButton> listButtonsRow = new ArrayList<>();
			InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
			for (String bottone : elencoBottoni) {
				if (conta>4) {
					conta=0;
					listOfListButton.add(listButtonsRow);
					listButtonsRow = new ArrayList<>();
				}
				conta++;
				inlineKeyboardButton = new InlineKeyboardButton();
				inlineKeyboardButton.setText(bottone);
				inlineKeyboardButton.setCallbackData("dettaglio " + bottone);
				listButtonsRow.add(inlineKeyboardButton);
			}
			listOfListButton.add(listButtonsRow);
			inlineKeyboardMarkup.setKeyboard(listOfListButton);
			sendMessage.setReplyMarkup(inlineKeyboardMarkup);
		}
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
		String val = "2022-01-06T11:30:00Z";
		ZonedDateTime zoneDateTime = ZonedDateTime.parse(val, DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")));
		String data = zoneDateTime.format(DateTimeFormatter.ofPattern("E dd/MM/yyyy HH:mm").withZone(ZoneId.of("Europe/Rome")));
		System.out.println(data);

		Constant c=null;
		Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
		Method method = cl.getDeclaredMethod("constant");
		c = (Constant) method.invoke(c);		
		RisultatiConRitardoBOT bot = inizializza();
		try {
			bot.inviaMessaggio(c.CHAT_ID_FANTALIVE, "PROVA... Status:STARTED");
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		Main.init(null,null,c, false);
		String testoCallback = (String) Main.getOldSnapPartite(true).get("testo");
		System.out.println(testoCallback);
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
