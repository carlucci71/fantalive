package com.daniele.asta.service;

import com.daniele.fantalive.entity.EnumCategoria;
import com.daniele.fantalive.entity.LoggerMessaggi;
import com.daniele.fantalive.repository.LoggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AstaMessageLogService {

    public static final int MAX_MESSAGGI_BROADCAST = 100;

    private final List<Map<String, Object>> messaggi = new CopyOnWriteArrayList<>();

    @Autowired
    private LoggerRepository loggerRepository;

    public List<Map<String, Object>> getMessaggi() {
        return messaggi;
    }

    /** Ultimi N messaggi per broadcast evento (S4.4). */
    public List<Map<String, Object>> getMessaggiForBroadcast() {
        int size = messaggi.size();
        if (size <= MAX_MESSAGGI_BROADCAST) {
            return messaggi;
        }
        return new ArrayList<>(messaggi.subList(size - MAX_MESSAGGI_BROADCAST, size));
    }

    public void clearMessaggi() {
        messaggi.clear();
    }

    /** Aggiunge al buffer in memoria (sync) e persiste su DB in async. */
    public void creaMessaggio(String indirizzo, String messaggio, EnumCategoria categoria) {
        long now = System.currentTimeMillis();
        Map<String, Object> msg = new HashMap<>();
        msg.put("key", UUID.randomUUID().toString());
        msg.put("data", now);
        msg.put("testo", messaggio);
        msg.put("indirizzo", indirizzo);
        msg.put("categoria", categoria);
        messaggi.add(msg);

        LoggerMessaggi entity = new LoggerMessaggi();
        entity.setId(now);
        entity.setMessaggio(messaggio);
        entity.setCategoria(categoria.name());
        entity.setIndirizzo(indirizzo);
        persistAsync(entity);
    }

    @Async
    public void persistAsync(LoggerMessaggi entity) {
        loggerRepository.save(entity);
    }
}
