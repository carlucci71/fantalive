package com.daniele.fantalive.bl;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.daniele.fantalive.model.Return;
import com.daniele.fantalive.model.Squadra;
import com.daniele.fantalive.util.Constant;

public class FantaLiveBOT extends TelegramLongPollingBot{

	private static final String SIMULATA = "simulata";
	private static final String FILTRO = "filtro";
	private static final String PROIEZIONI = "proiezioni";
	private static final String CAMPIONATI = "campionati";
	private static final String LIVE = "@li";
	private static final String DETTAGLIO = "@dt";
	private static BotSession registerBot;
	private static String CHI;
	private Set<Long> ricercheGiocatori=new HashSet<>();
	private static FantaLiveBOT fantaLiveBOT;

	
	/*
giocatori - seleziona un giocatore
campionati - seleziona un campionato
proiezioni - visualizza proiezioni campionati
simulazioni - simula incontri
viskeepalive - visualizza keep alive
switchkeepalive - switch keep alive
	 */
	
	
	private synchronized SendMessage setButtonsGiocatori(long chatId, String filtro) throws Exception {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(Long.toString(chatId));
		// Create a keyboard
		InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
		sendMessage.setReplyMarkup(replyKeyboardMarkup);
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		Set<String> elencoGiocatori = Main.getElencoGiocatori(filtro);
		for (String giocatore : elencoGiocatori) {
	        List<InlineKeyboardButton> rowInline = new ArrayList<>();
	        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
	        inlineKeyboardButton.setText(giocatore);
	        inlineKeyboardButton.setCallbackData(FILTRO + " " + giocatore);
	        rowInline.add(inlineKeyboardButton);
	        rowsInline.add(rowInline);
		}
		// and assign this list to our keyboard
		replyKeyboardMarkup.setKeyboard(rowsInline);
		sendMessage.setReplyMarkup(replyKeyboardMarkup);
		sendMessage.setText("seleziona il giocatore");
		return sendMessage;
	}	
	
	private synchronized SendMessage setButtonsGiocatoriORIG(long chatId, String filtro) throws Exception {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.enableMarkdown(true);

		sendMessage.setChatId(Long.toString(chatId));
		// Create a keyboard
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		sendMessage.setReplyMarkup(replyKeyboardMarkup);
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboard(false);

		// Create a list of keyboard rows
		List<KeyboardRow> keyboard = new ArrayList<>();

		Set<String> elencoGiocatori = Main.getElencoGiocatori(filtro);
		for (String giocatore : elencoGiocatori) {
			KeyboardRow kbRiga = new KeyboardRow();
			KeyboardButton kbGiocatore = new KeyboardButton(giocatore);
			kbRiga.add(kbGiocatore);
			keyboard.add(kbRiga);
		}
		
		
		// and assign this list to our keyboard
		replyKeyboardMarkup.setKeyboard(keyboard);
		sendMessage.setReplyMarkup(replyKeyboardMarkup);
		sendMessage.setText("seleziona il giocatore");
		return sendMessage;
	}	
	

	@Override
	public void onUpdateReceived(Update update) {
		try {
			if(update.hasMessage()){
				Long chatId = update.getMessage().getChatId();
				String text = update.getMessage().getText();
				if(update.getMessage().hasText()){
					if(text.equals("/viskeepalive")){
						Map<String, String> visKeepAliveEnd = Main.visKeepAliveEnd();
						inviaMessaggio(chatId, visKeepAliveEnd.get("VIS_KEEP_ALIVE"), false);
					}
					else if(text.equals("/switchkeepalive")){
						Map<String, String> visKeepAliveEnd = Main.visKeepAliveEnd();
						Map<String, Object> body=new HashMap<String, Object>();
						body.put("verso", (visKeepAliveEnd.get("VIS_KEEP_ALIVE").equals("S")?false:true));
						Map<String, Object> setKeepAliveEnd = Main.setKeepAliveEnd(body);
						inviaMessaggio(chatId, setKeepAliveEnd.get("VIS_KEEP_ALIVE").toString(), false);
					}
					else if(text.equals("/campionati")){
						execute(sendInlineKeyBoardCampionati(chatId,CAMPIONATI + " ",false,text));
					}
					else if(text.equals("/proiezioni")){
						execute(sendInlineKeyBoardCampionati(chatId,PROIEZIONI + " ",true,text));
					}
					else if(text.equals("/simulazioni")){
						execute(sendInlineKeyBoardPartiteSimulate(chatId,"simula" + " ",text));
					}
					else if(text.equals("/giocatori")){
						execute(sendInlineKeyBoardGiocatori(chatId,"giocatori" + " ",text));
					}
					else {
						if (ricercheGiocatori.contains(chatId)) {
							execute(setButtonsGiocatori(chatId,text));
							ricercheGiocatori.remove(chatId);
						}
						else if (text.equals("killMe")) {
							fantaLiveBOT.stopBot();
						}
						else {
							execute(creaSendMessage(chatId,text, true));//REPLY
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
				if (testoCallback.startsWith(CAMPIONATI)) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					execute(sendInlineKeyBoardSquadre(chatId,testoCallback, null,testoCallback, null, null));
				}
				else if (testoCallback.startsWith(PROIEZIONI)) {
					String campionato =testoCallback.substring(testoCallback.indexOf(" ")+1);
					Map<String, Object> proiezioni = Main.proiezioni(campionato);
					execute(sendInlineKeyBoardSquadre(chatId,campionato, (List<String>) proiezioni.get("squadre"),(String) proiezioni.get("testo"),null, null));
				}
				else if (testoCallback.startsWith(DETTAGLIO)) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					String[] split = testoCallback.split("#");
					inviaMessaggio(chatId,Main.getDettaglio(chatId,split[0],split[1],split[2],split[3], false), false);
				}
				else if (testoCallback.startsWith(LIVE)) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					String[] split = testoCallback.split("#");
					inviaMessaggio(chatId,Main.getDettaglio(chatId,split[0],split[1],split[2],split[3], true), false);
				}
				else if (testoCallback.startsWith(FILTRO)) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					String dettaglioGiocatore = Main.getDettaglioGiocatore(testoCallback);
					execute(creaSendMessage(chatId,dettaglioGiocatore, false));

				}
				else if (testoCallback.startsWith(SIMULATA)) {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					Map<String, Object> proiezioni = Main.getPartitaSimulata(chatId,testoCallback, null);
					execute(sendInlineKeyBoardSquadre(chatId,(String) proiezioni.get("campionato"), (List<String>) proiezioni.get("squadre"),(String) proiezioni.get("testo"),(List<String>) proiezioni.get("squadreCasa"),testoCallback));
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
		
		Main.init(null,null,c, false, "8080", null);
		FantaLiveBOT bot = inizializza("MAIN");
		
		try {
			bot.inviaMessaggio(c.CHAT_ID_FANTALIVE, "Status:STARTED", false);
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		System.err.println("FINE");
	}

	public static FantaLiveBOT inizializza(String chi) throws Exception {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		fantaLiveBOT = new FantaLiveBOT();
		registerBot=telegramBotsApi.registerBot(fantaLiveBOT);
		CHI=chi;
		return fantaLiveBOT;
	}

	public void inviaMessaggio(long chatId,String msg,boolean bReply) throws TelegramApiException {
		try {
			while (msg.length()>4000) {
				execute(creaSendMessage(chatId, msg.substring(0,4000),bReply));
				msg=msg.substring(4000);
			}
			
			execute(creaSendMessage(chatId, msg,bReply));
			
		} catch (TelegramApiException e) {
			throw e;
		}
	}
	private SendMessage sendInlineKeyBoardGiocatori(long chatId, String cb, String testo){
		ricercheGiocatori.add(chatId);
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.setChatId(Long.toString(chatId));
		sendMessage.setText("Digita il giocatore che vuoi ricercare");
		return sendMessage;
	}
	private SendMessage sendInlineKeyBoardPartiteSimulate(long chatId, String cb, String testo) throws Exception{
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoPartiteSimulate());
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.setChatId(Long.toString(chatId));
		sendMessage.setText(testo);
		sendMessage.setReplyMarkup(inlineKeyboardMarkup);
		return sendMessage;
	}
	private SendMessage sendInlineKeyBoardCampionati(long chatId, String testoCallBack, boolean all, String testo){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoCampionati(testoCallBack,all));
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.setChatId(Long.toString(chatId));
		sendMessage.setText(testo);
		sendMessage.setReplyMarkup(inlineKeyboardMarkup);
		return sendMessage;
	}
	private SendMessage sendInlineKeyBoardSquadre(long chatId, String campionato, List<String> squadrePuntuali, String testo, List<String> simulazioneListaCasa, String nomePartitaSimulata){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoSquadre(campionato,squadrePuntuali, simulazioneListaCasa, nomePartitaSimulata));
		SendMessage sendMessage =  new SendMessage();
		sendMessage.enableHtml(true);
		sendMessage.setParseMode("html");
		sendMessage.setChatId(Long.toString(chatId));
		sendMessage.setText(testo);
		sendMessage.setReplyMarkup(inlineKeyboardMarkup);
		return sendMessage;
	}
	private List<List<InlineKeyboardButton>> generaElencoSquadre(String campionato, List<String> squadrePuntuali, List<String> simulazioneListaCasa, String nomePartitaSimulata) {
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
				List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
				List<Squadra> squadre = return1.getSquadre();
				for (Squadra squadra : squadre) {
					if (squadrePuntuali == null || squadrePuntuali.contains(attCamp + "-" + squadra.getNome())) {
						String casa;
						if (simulazioneListaCasa==null) {
							if (squadra.isCasaProiezione()) {
								casa = "S";
							} else {
								casa="N";
							}
						} else {
							if (simulazioneListaCasa.contains(squadra.getNome())){
								casa = "S";
							} else {
								casa="N";
							}
						}
						InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
						keyboardButtonsRow = new ArrayList<>();
						inlineKeyboardButton.setText(squadra.getNome());
						inlineKeyboardButton.setCallbackData(DETTAGLIO + " " + attCamp + "#"+ squadra.getNome()+ "#"+ casa+"#"+ (nomePartitaSimulata==null?"-":nomePartitaSimulata));
						keyboardButtonsRow.add(inlineKeyboardButton);
//						rowList.add(keyboardButtonsRow);
						inlineKeyboardButton = new InlineKeyboardButton();
//						keyboardButtonsRow = new ArrayList<>();
						inlineKeyboardButton.setText("live " + squadra.getNome());
						inlineKeyboardButton.setCallbackData(LIVE + " " + attCamp + "#"+ squadra.getNome()+ "#"+ casa+"#"+ (nomePartitaSimulata==null?"-":nomePartitaSimulata));
						keyboardButtonsRow.add(inlineKeyboardButton);
						rowList.add(keyboardButtonsRow);
					}
				}
			}
			return rowList;
		}
		catch (Exception e ) {
			throw new RuntimeException(e);
		}
	}
	private List<List<InlineKeyboardButton>> generaElencoPartiteSimulate() throws Exception {
		try {
			List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
			Constant.Campionati[] campionati = Constant.Campionati.values();
			for (Constant.Campionati campionato : campionati) {
				List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
				try {
					Set<String> getpartiteSimulate = Main.getpartiteSimulate(campionato.name());
					for (String partitaSimulata : getpartiteSimulate) {
						InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
						inlineKeyboardButton.setText(partitaSimulata.substring(2));
						inlineKeyboardButton.setCallbackData(SIMULATA + " " + partitaSimulata);
						keyboardButtonsRow1.add(inlineKeyboardButton);
					}
					rowList.add(keyboardButtonsRow1);
				}
				catch (Exception e) {
					
				}
			}
			return rowList;
		}
		catch (Exception e ) {
			throw e;
		}
	}
	private List<List<InlineKeyboardButton>> generaElencoCampionati(String cb, boolean all) {
		try {
			List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
			Constant.Campionati[] campionati = Constant.Campionati.values();
			List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
			if (all) {
				InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
				inlineKeyboardButton.setText("ALL");
				inlineKeyboardButton.setCallbackData(cb + "ALL");
				keyboardButtonsRow1.add(inlineKeyboardButton);
			}
			for (Constant.Campionati campionato : campionati) {
				InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
				inlineKeyboardButton.setText(campionato.name());
				inlineKeyboardButton.setCallbackData(cb + campionato.name());
				keyboardButtonsRow1.add(inlineKeyboardButton);
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
