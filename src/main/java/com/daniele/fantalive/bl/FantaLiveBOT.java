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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
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

	@Override
	public void onUpdateReceived(Update update) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getBotUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return null;
	}

	public static FantaLiveBOT inizializza(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public void inviaMessaggio(Long cHAT_ID_FANTALIVE, String msg, boolean b) {
		// TODO Auto-generated method stub
		
	}

	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	public void startBot() {
		// TODO Auto-generated method stub
		
	}

	public void stopBot() {
		// TODO Auto-generated method stub
		
	}

}
