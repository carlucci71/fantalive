package fantalive.bl;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import fantalive.bl.Main.Campionati;
import fantalive.model.Return;
import fantalive.model.Squadra;
import fantalive.util.Constant;

public class FantaLiveBOT extends TelegramLongPollingBot{

	private static BotSession registerBot;
	private static String CHI;
	


	@Override
	public void onUpdateReceived(Update update) {
		if(update.hasMessage()){
			Long chatId = update.getMessage().getChatId();
			String text = update.getMessage().getText();
			if(update.getMessage().hasText()){
				if(update.getMessage().getText().equals("/campionati")){
					try {
						execute(sendInlineKeyBoardCampionati(chatId,text, "analizza ",false));//COMANDO CAMPIONATO
					} catch (TelegramApiException e) {
						throw new RuntimeException(e);
					}
				}
				else if(update.getMessage().getText().equals("/proiezioni")){
					try {
						execute(sendInlineKeyBoardCampionati(chatId,text, "proietta ",true));//COMANDO PROIEZIONI
//						inviaMessaggio(chatId,Main.proiezioni(), false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				else {
					try {
						inviaMessaggio(chatId,text, true);//REPLY
					} catch (TelegramApiException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}else if(update.hasCallbackQuery()){
			Long chatId = update.getCallbackQuery().getMessage().getChatId();
			String testoCallback = update.getCallbackQuery().getData();
			if (testoCallback.startsWith("analizza")) {
				try {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					execute(sendInlineKeyBoardSquadre(chatId,testoCallback));//COMANDO ANALIZZA CAMPIONATO
				} catch (TelegramApiException e) {
					throw new RuntimeException(e);
				}
			}
			else if (testoCallback.startsWith("proietta")) {
				try {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					inviaMessaggio(chatId,Main.proiezioni(testoCallback), false);//PROIETTA SQUADRA
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else if (testoCallback.startsWith("dettaglio")) {
				try {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					String[] split = testoCallback.split("#");
					inviaMessaggio(chatId,Main.getDettaglio(chatId,split[0],split[1]), false);//SQUADRA
				} catch (TelegramApiException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				try {
					inviaMessaggio(chatId, "cosa hai mandato? " + testoCallback , false);
				} catch (TelegramApiException e) {
					throw new RuntimeException(e);
				}
			}
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
		
		Class<?> cl = Class.forName("fantalive.util.ConstantDev");
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
			execute(creaSendMessage(chatId, msg , bReply));//INVIA
		} catch (TelegramApiException e) {
			throw e;
		}
	}
	

	private SendMessage sendInlineKeyBoardCampionati(long chatId, String msg, String cb, boolean all){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoCampionati(cb,all));
		return new SendMessage().setChatId(chatId).setText(msg).setReplyMarkup(inlineKeyboardMarkup);
	}

	private SendMessage sendInlineKeyBoardSquadre(long chatId, String msg){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoSquadre(msg));
		return new SendMessage().setChatId(chatId).setText(msg).setReplyMarkup(inlineKeyboardMarkup);
	}


	private List<List<InlineKeyboardButton>> generaElencoSquadre(String campionato) {
		try {
			List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
			Map<String, Return> go = Main.go(false, null, null);
			Return return1 = go.get(campionato);
			List<Squadra> squadre = return1.getSquadre();
			for (Squadra squadra : squadre) {
				List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
				keyboardButtonsRow1.add(new InlineKeyboardButton().setText(squadra.getNome()).setCallbackData("dettaglio " + campionato + "#"+ squadra.getNome()));
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
