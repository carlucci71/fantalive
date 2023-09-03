package com.daniele.fantalive.configurazione;

import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
public class MyHttpSessionListener implements ApplicationListener<SessionDestroyedEvent> {

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
    	System.out.println("***********************");
    	System.out.println(event);
        // Gestisci l'evento di sessione distrutta qui
    }
}