package com.daniele.asta.service;

import com.daniele.asta.MyControllerAsta;
import com.daniele.fantalive.entity.Allenatori;
import com.daniele.fantalive.entity.Configurazione;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AstaTurnoService {

    @Autowired
    private MyControllerAsta myController;

    public void avanzaTurnoDopoConferma(Map<String, Object> broadcastPayload) {
        Integer iTurno = Integer.parseInt(myController.getTurno());
        Iterable<Allenatori> allAllenatori = myController.getAllAllenatori();
        List<Map<String, Object>> riepilogoAllenatori = myController.riepilogoAllenatori();
        Configurazione configurazione = myController.getConfigurazione();
        boolean okTurno = false;
        int contaPassaggi = 0;
        while (!okTurno) {
            iTurno++;
            String nomeFirst = null;
            int conta = 0;
            for (Allenatori allenatori : allAllenatori) {
                if (nomeFirst == null) {
                    nomeFirst = allenatori.getNome();
                }
                if (allenatori.getOrdine() == iTurno) {
                    myController.setNomeGiocatoreTurno(allenatori.getNome());
                }
                conta++;
            }
            if (iTurno > conta - 1) {
                iTurno = 0;
                myController.setNomeGiocatoreTurno(nomeFirst);
            }
            List<Map<String, Object>> attAllenatore = riepilogoAllenatori.stream()
                    .filter((kk) -> kk.get("nome").equals(myController.getNomeGiocatoreTurno()))
                    .collect(Collectors.toList());

            Map<String, Object> attP = attAllenatore.stream()
                    .filter((kk) -> kk.get("ruolo").equals("P"))
                    .findFirst()
                    .orElse(new HashMap<>());
            Map<String, Object> attD = attAllenatore.stream()
                    .filter((kk) -> kk.get("ruolo").equals("D"))
                    .findFirst()
                    .orElse(new HashMap<>());
            Map<String, Object> attC = attAllenatore.stream()
                    .filter((kk) -> kk.get("ruolo").equals("C"))
                    .findFirst()
                    .orElse(new HashMap<>());
            Map<String, Object> attA = attAllenatore.stream()
                    .filter((kk) -> kk.get("ruolo").equals("A"))
                    .findFirst()
                    .orElse(new HashMap<>());

            int contaP = toInt(attP.get("conta"));
            int contaD = toInt(attD.get("conta"));
            int contaC = toInt(attC.get("conta"));
            int contaA = toInt(attA.get("conta"));

            if (contaP != configurazione.getMaxP().intValue()
                    || contaD != configurazione.getMaxD().intValue()
                    || contaC != configurazione.getMaxC().intValue()
                    || contaA != configurazione.getMaxA().intValue()) {
                okTurno = true;
            }
            contaPassaggi++;
            if (configurazione.getNumeroGiocatori() < contaPassaggi) {
                okTurno = true;
            }
        }
        myController.setTurno(Integer.toString(iTurno));
        broadcastPayload.put("turno", myController.getTurno());
        broadcastPayload.put("giocatoriPerSquadra", myController.giocatoriPerSquadra());
        broadcastPayload.put("mapSpesoTotale", myController.getMapSpesoTotale());
        broadcastPayload.put("nomeGiocatoreTurno", myController.getNomeGiocatoreTurno());
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
