package com.daniele.fantalive.bl;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.checkerframework.framework.qual.InvisibleQualifier;
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

import com.daniele.fantalive.bl.Main.Campionati;
import com.daniele.fantalive.model.Return;
import com.daniele.fantalive.model.Squadra;
import com.daniele.fantalive.util.Constant;

public class FantaCronacaLiveBOT {

	public void stopBot() {
		// TODO Auto-generated method stub
		
	}

	public void startBot() {
		// TODO Auto-generated method stub
		
	}

	public void inviaMessaggio(Long cHAT_ID_FANTALIVE, String msg) {
		// TODO Auto-generated method stub
		
	}

	public static FantaCronacaLiveBOT inizializza(String string) {
		// TODO Auto-generated method stub
		return null;
	}


}
