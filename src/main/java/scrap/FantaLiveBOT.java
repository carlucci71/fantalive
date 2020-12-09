package scrap;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import scrap.Main.Campionati;

public class FantaLiveBOT extends TelegramLongPollingBot{

	private static final int CHAT_ID_FANTALIVE = 425497266;
	private static final String TOKEN_BOT_FANTALIVE = "1363620575:AAEcdK-zRf1uZZu3SlDkFsBtD2s8jdU-oeU";
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
						execute(sendInlineKeyBoardCampionati(chatId,text));
					} catch (TelegramApiException e) {
						throw new RuntimeException(e);
					}
				}
				else {
					try {
						execute(sendMessage(chatId,text, true));
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
					execute(sendInlineKeyBoardSquadre(chatId,testoCallback));
				} catch (TelegramApiException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				try {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					String[] split = testoCallback.split("#");
					execute(new SendMessage().setText(getDettaglio(split[0],split[1])).setChatId(chatId));
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
		return TOKEN_BOT_FANTALIVE;
	}

	public static void main(String[] args) throws Exception {
		Main.init();
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
		f.messaggioBenvenuto();
		return f;
	}

	public void inviaMessaggio(String msg,boolean bReply) throws Exception {
		try {
			execute(sendMessage(CHAT_ID_FANTALIVE, msg , bReply));
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void messaggioBenvenuto() throws Exception {
		try {
			inviaMessaggio("BOT AVVIATO", false);
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	private SendMessage sendInlineKeyBoardCampionati(long chatId, String msg){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoCampionati());
		return new SendMessage().setChatId(chatId).setText(msg).setReplyMarkup(inlineKeyboardMarkup);
	}

	private SendMessage sendInlineKeyBoardSquadre(long chatId, String msg){
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		inlineKeyboardMarkup.setKeyboard(generaElencoSquadre(msg));
		return new SendMessage().setChatId(chatId).setText(msg).setReplyMarkup(inlineKeyboardMarkup);
	}

	private String getDettaglio(String campionato, String squadra){
		try {
			Map<String, Return> go = Main.go(true, null, null);
			Return return1 = go.get(campionato);
			List<Squadra> squadre = return1.getSquadre();
			for (Squadra sq : squadre) {
				if (sq.getNome().equalsIgnoreCase(squadra)) {
					return sq.toString(); 
				}
			}
			return ""; 
		}
		catch (Exception e ) {
			throw new RuntimeException(e);
		}
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

	private List<List<InlineKeyboardButton>> generaElencoCampionati() {
		try {
			List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
			Campionati[] campionati = Main.Campionati.values();
			for (Campionati campionato : campionati) {
				List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
				keyboardButtonsRow1.add(new InlineKeyboardButton().setText(campionato.name()).setCallbackData("analizza " + campionato.name()));
				rowList.add(keyboardButtonsRow1);
			}
			return rowList;
		}
		catch (Exception e ) {
			throw new RuntimeException(e);
		}
	}

	private SendMessage sendMessage(long chatId,String msg, boolean bReply) {
		SendMessage sendMessage = new SendMessage();
//		AGGIUNGE TASTIERA		
//		sendMessage.enableMarkdown(true);
//		setButtons(sendMessage);
//		RIMUOVE TASTIERA		
//		ReplyKeyboardRemove replyKeyboardMarkup = new ReplyKeyboardRemove();
//		sendMessage.setReplyMarkup(replyKeyboardMarkup);

		sendMessage.setChatId(chatId);
		String messaggio="";
		if (bReply) {
			messaggio="sono il bot reply da " + chatId + " ";
		}
		try {
			messaggio = messaggio  + CHI + " " + InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		messaggio = messaggio + " " + msg;
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
