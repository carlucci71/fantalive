package com.daniele.fantalive.util;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.daniele.fantalive.bl.FantaCronacaLiveBOT;
import com.daniele.fantalive.bl.FantaLiveBOT;
import com.daniele.fantalive.bl.RisultatiConRitardoBOT;

public class ThreadSeparato implements Runnable{

	private TelegramLongPollingBot telegramLongPollingBot;
	private Long CHAT_ID_FANTALIVE;
	private String msg;

	public ThreadSeparato(TelegramLongPollingBot telegramLongPollingBot, Long cHAT_ID_FANTALIVE, String msg) {
		super();
		this.telegramLongPollingBot = telegramLongPollingBot;
		CHAT_ID_FANTALIVE = cHAT_ID_FANTALIVE;
		this.msg = msg;
	}


	public void run() {
		try {
			if (telegramLongPollingBot instanceof FantaLiveBOT) {
				((FantaLiveBOT)telegramLongPollingBot).inviaMessaggio(CHAT_ID_FANTALIVE,msg,false);
			}
			if (telegramLongPollingBot instanceof RisultatiConRitardoBOT) {
				((RisultatiConRitardoBOT)telegramLongPollingBot).inviaMessaggio(CHAT_ID_FANTALIVE,msg);
			}
			if (telegramLongPollingBot instanceof FantaCronacaLiveBOT) {
				((FantaCronacaLiveBOT)telegramLongPollingBot).inviaMessaggio(CHAT_ID_FANTALIVE,msg);
			}
		}
		catch (Exception e) {
			System.out.println("non sono riuscito ad eseguire il thread");
			e.printStackTrace(System.out);
		}

	}

}
