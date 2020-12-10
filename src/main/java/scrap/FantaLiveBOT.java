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

	private static BotSession registerBot;
	private static String CHI;
	private final  String FINITO = "\u2709";
	private final  String CLESSIDRA  = "\u23F3";
	private final  String CON_VOTO = "\u2714";
	private final  String SENZA_VOTO = "\u2753";
	private final  String SQUADRA_GIOCA = "\u26BD";
	private final  String NON_GIOCA  = "\u23F0";//274E
	private static String MIO_IP;
	

	@Override
	public void onUpdateReceived(Update update) {
		if(update.hasMessage()){
			Long chatId = update.getMessage().getChatId();
			String text = update.getMessage().getText();
			if(update.getMessage().hasText()){
				if(update.getMessage().getText().equals("/campionati")){
					try {
						execute(sendInlineKeyBoardCampionati(chatId,text));//COMANDO CAMPIONATO
					} catch (TelegramApiException e) {
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
			else {
				try {
					testoCallback =testoCallback.substring(testoCallback.indexOf(" ")+1);
					String[] split = testoCallback.split("#");
					inviaMessaggio(chatId,getDettaglio(chatId,split[0],split[1]), false);//SQUADRA
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
		MIO_IP = InetAddress.getLocalHost().getHostAddress();
		f.messaggioBenvenuto();
		return f;
	}

	public void inviaMessaggio(long chatId,String msg,boolean bReply) throws TelegramApiException {
		try {
			execute(creaSendMessage(chatId, msg , bReply));//INVIA
		} catch (TelegramApiException e) {
			throw e;
		}
	}
	
	private void messaggioBenvenuto() throws Exception {
			inviaMessaggio(Constant.CHAT_ID_FANTALIVE, "BOT AVVIATO", false);
			
			inviaMessaggio(Constant.CHAT_ID_FANTALIVE, getDettaglio(Constant.CHAT_ID_FANTALIVE,"BE", "tavolino"), false);
			
			
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

	private String getDettaglio(Long chatId, String campionato, String squadra){
		try {
			Map<String, Return> go = Main.go(true, null, null);
			Return return1 = go.get(campionato);
			List<Squadra> squadre = return1.getSquadre();
			for (Squadra sq : squadre) {
				if (sq.getNome().equalsIgnoreCase(squadra)) {
					StringBuilder testo = new StringBuilder();
					testo.append("\n<b>").append(sq.getNome()).append("</b> --> <b><i>").append(sq.getProiezione()).append("</i></b>\n\n");
					for (Giocatore giocatore : sq.getTitolari()) {
						dettaglioTestoGiocatore(testo, giocatore,campionato);
					}
					testo.append("\n");
					testo.append("Giocatori con voto: ").append(sq.getContaTitolari()).append("\n");
					testo.append("Media votati: ").append(sq.getMediaTitolari()).append("\n");
					testo.append("Ancora da giocare: ").append(sq.getContaSquadraTitolariNonGioca()).append("\n");
					testo.append("Totale: ").append(sq.getTotaleTitolari()).append("\n");
					testo.append("\n");
					for (Giocatore giocatore : sq.getRiserve()) {
						dettaglioTestoGiocatore(testo, giocatore,campionato);
					}
					testo.append("\n");
					testo.append("Giocatori con voto: ").append(sq.getContaRiserve()).append("\n");
					testo.append("Ancora da giocare: ").append(sq.getContaSquadraRiserveNonGioca()).append("\n");
					
					if(chatId == Constant.CHAT_ID_FANTALIVE) {
						testo.append("\n").append(Main.URL_NOTIFICA);
					}
					
					
//					System.err.println(testo.toString());
					return testo.toString(); 
				}
			}
			return ""; 
		}
		catch (Exception e ) {
			throw new RuntimeException(e);
		}
	}

	private void dettaglioTestoGiocatore(StringBuilder testo, Giocatore giocatore, String campionato) {
		testo.append(giocatore.getIdGioc()).append("\t");
		testo.append(partitaFinita(giocatore)).append(conVoto(giocatore)).append(squadraGioca(giocatore)).append("  ");
		testo.append("<b>").append(giocatore.getNome()).append("</b>").append("\t");
		testo.append(giocatore.getSquadra()).append("\t");
		testo.append(giocatore.getRuolo()).append("\t");
		testo.append(getVoto(giocatore)).append("\t");
		for (Integer evento : giocatore.getCodEventi()) {
			testo.append(desEvento(evento,campionato)).append("  ");
		}
		testo.append("<b>").append(getFantaVoto(giocatore)).append("</b>");
		if (giocatore.isCambiato()) testo.append("(*)");
		testo.append("\t");
		testo.append(getOrario(giocatore.getOrario()));
		testo.append("\n");
	}
	private String desEvento(Integer ev,String r){
		String[] evento = Main.eventi.get(ev);
		String ret = evento[0];
		String valEvento = valEvento(evento,r);
		if (!"0".equals(valEvento)) {
			ret = ret + " (" + valEvento + ") "; 
		}
		return ret;
	}
	private String valEvento(String[] evento,String r){
		int pos=0;
		if (r.equals("FANTAVIVA")) pos=1;
		if (r.equals("LUCCICAR")) pos=2;
		if (r.equals("BE")) pos=3;
		return evento[pos];
	}

	private String partitaFinita(Giocatore giocatore){
		if (giocatore.getOrario().get("tag").equals("FullTime")) return FINITO;
		if (giocatore.getCodEventi().contains(14)) return FINITO;
		return CLESSIDRA;
	}
	private String conVoto(Giocatore giocatore){
		if (giocatore.getVoto()==0) return SENZA_VOTO;
		return CON_VOTO;
	}
	private String squadraGioca(Giocatore giocatore){
		if (giocatore.isSquadraGioca()) return SQUADRA_GIOCA;
		return NON_GIOCA;
	}
	private String getVoto(Giocatore g) {
		if (!g.isSquadraGioca()) return " ";
		if (g.getVoto()==0) return "NV";
		return String.valueOf(g.getVoto());
	}
	private String getFantaVoto(Giocatore g) {
		if (!g.isSquadraGioca()) return " ";
		if (g.getVoto()==0) return "NV";
		return String.valueOf(g.getVoto()+g.getModificatore());
	}
	private String getOrario(Map<String,String> orario){
		String tag = orario.get("tag");
		if (tag.equals("FullTime") || tag.equals("Postponed") || tag.equals("Cancelled") || tag.equals("Walkover")) return tag;
		if (tag.equals("PreMatch")){
			String ret="";
			ret = ret + orario.get("val").substring(8,10);
			ret = ret + "/" + orario.get("val").substring(5,7);
			ret = ret + " " + (1+Integer.parseInt(orario.get("val").substring(11,13)));
			ret = ret + ":" + orario.get("val").substring(14,16);
			return ret;
		}
		return orario.get("val") + "Min";
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
		if (bReply) {
			messaggio="<b>sono il bot reply</b> da  " + chatId + " ";
		}
		
		messaggio = messaggio + " " + msg;

		if (chatId == Constant.CHAT_ID_FANTALIVE) {
			messaggio = messaggio + "\n\n<i>" + CHI + " " + MIO_IP + "</i>";
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
