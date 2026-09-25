package com.daniele.asta.service;

import com.daniele.asta.MyControllerAsta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Query DB per payload WS — sync ma isolate dal handler (S3.2). */
@Service
public class AstaDataService {

    @Autowired
    private MyControllerAsta myController;

    public List<Map<String, Object>> getGiocatoriLiberi() {
        return myController.getGiocatoriLiberi();
    }

    public List<Map<String, Object>> elencoCronologiaOfferte() {
        return myController.elencoCronologiaOfferte();
    }

    public Map<String, Object> buildConnettiPayload(String idgiocatore) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("calciatori", getGiocatoriLiberi());
        myController.aggiornaFavoriti(idgiocatore);
        data.put("preferiti", myController.getFavoriti());
        data.put("cronologiaOfferte", elencoCronologiaOfferte());
        return data;
    }

    public Map<String, Object> buildPostConfirmPayload() {
        Map<String, Object> data = new HashMap<>();
        data.put("calciatori", getGiocatoriLiberi());
        data.put("cronologiaOfferte", elencoCronologiaOfferte());
        return data;
    }
}
