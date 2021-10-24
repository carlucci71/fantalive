package com.daniele.fantalive.util;

import com.daniele.fantalive.bl.FantaLiveBOT;

public class ThreadSeparato implements Runnable{

	private FantaLiveBOT fantaLiveBot;
	private Long CHAT_ID_FANTALIVE;
	private String msg;

	public ThreadSeparato(FantaLiveBOT fantaLiveBot, Long cHAT_ID_FANTALIVE, String msg) {
		super();
		this.fantaLiveBot = fantaLiveBot;
		CHAT_ID_FANTALIVE = cHAT_ID_FANTALIVE;
		this.msg = msg;
	}


	public void run() {
		try {
			fantaLiveBot.inviaMessaggio(CHAT_ID_FANTALIVE,msg,false);
		}
		catch (Exception e) {
			System.out.println("non sono riuscito ad eseguire il thread");
		}

	}

}
