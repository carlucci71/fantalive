package com.daniele.fantalive.util;

import com.daniele.fantalive.bl.FantaCronacaLiveBOT;
import com.daniele.fantalive.bl.FantaLiveBOT;
import com.daniele.fantalive.bl.Main;
import com.daniele.fantalive.bl.RisultatiConRitardoBOT;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ThreadSeparato implements Runnable {

    private TelegramLongPollingBot telegramLongPollingBot;
    private Long CHAT_ID_FANTALIVE;
    private String msg;

    public Instant getOraInvio() {
        return oraInvio;
    }

    public void setOraInvio(Instant oraInvio) {
        this.oraInvio = oraInvio;
    }

    Instant oraInvio;
    String uuid;

    public ThreadSeparato(TelegramLongPollingBot telegramLongPollingBot, Long chatIdFantalive, String msg, Instant oraInvio, String uuid) {
        super();
        this.oraInvio = oraInvio;
        this.uuid = uuid;
        this.telegramLongPollingBot = telegramLongPollingBot;
        CHAT_ID_FANTALIVE = chatIdFantalive;
        this.msg = msg;
    }


    public void run() {
        try {
            while (oraInvio.isAfter(Instant.now())) {
                TimeUnit.SECONDS.sleep(1);
            }
            if (telegramLongPollingBot instanceof FantaLiveBOT) {
                ((FantaLiveBOT) telegramLongPollingBot).inviaMessaggio(CHAT_ID_FANTALIVE, msg, false);
                Main.threadSeparatiInAttesa.remove(uuid);
            }
            if (telegramLongPollingBot instanceof RisultatiConRitardoBOT) {
                ((RisultatiConRitardoBOT) telegramLongPollingBot).inviaMessaggio(CHAT_ID_FANTALIVE, msg);
                Main.threadSeparatiInAttesa.remove(uuid);
            }
            if (telegramLongPollingBot instanceof FantaCronacaLiveBOT) {
                ((FantaCronacaLiveBOT) telegramLongPollingBot).inviaMessaggio(CHAT_ID_FANTALIVE, msg);
                Main.threadSeparatiInAttesa.remove(uuid);
            }

        } catch (Exception e) {
            System.out.println("non sono riuscito ad eseguire il thread");
            e.printStackTrace(System.out);
        }

    }

}
