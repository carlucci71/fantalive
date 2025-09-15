package com.daniele.fantalive.util;

import com.daniele.fantalive.bl.Main;
import com.daniele.fantalive.model.Giocatore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.daniele.fantalive.bl.Main.eventi;
import static com.daniele.fantalive.bl.Main.sq;


public class VerificaFS {

    public static void main(String[] args) throws Exception {
        VerificaFS cp = new VerificaFS();
        cp.init();
        cp.go();
        System.exit(0);
    }

    private void init() {
        sq = new LinkedHashMap<Integer, String>();
        sq.put(8, "GEN");
        sq.put(7, "FRO");
        sq.put(1, "ATA");
        sq.put(2, "BOL");
        sq.put(21, "CAG");
        sq.put(6, "FIO");
        sq.put(9, "INT");
        sq.put(10, "JUV");
        sq.put(11, "LAZ");
        sq.put(12, "MIL");
        sq.put(13, "NAP");
        sq.put(107, "PAR");
        sq.put(15, "ROM");
        sq.put(16, "SAM");
        sq.put(17, "SAS");
        sq.put(129, "SPE");
        sq.put(18, "TOR");
        sq.put(19, "UDI");
        sq.put(20, "VER");
        sq.put(137, "SAL");
        sq.put(144, "CRE");
        sq.put(119, "LEC");
        sq.put(138, "VEN");
        sq.put(153, "COM");
        sq.put(157, "PIS");


        eventi = new HashMap<>();
        eventi.put(1000, new String[]{"portiere imbattuto", "", "", "1", "", "S", Constant.IMBATTUTO, "portiere_imbattuto"});
        eventi.put(1, new String[]{"ammonito", "", "", "-0.5", "", "S", Constant.AMMONITO, "ammonizione"});
        eventi.put(2, new String[]{"espulso", "", "", "-1", "", "S", Constant.ESPULSO, "espulsione"});
        eventi.put(3, new String[]{"gol", "", "", "3", "", "S", Constant.GOL, "gol_segnato"});
        eventi.put(4, new String[]{"gol subito", "", "", "-1", "", "S", Constant.GOL_SUBITO, "gol_subito"});
        eventi.put(7, new String[]{"rigore parato", "", "", "3", "", "S", Constant.RIGORE_PARATO, "rigore_parato"});
        eventi.put(8, new String[]{"rigore sbagliato", "", "", "-3", "", "S", Constant.RIGORE_SBAGLIATO, "rigore_sbagliato"});
        eventi.put(9, new String[]{"rigore segnato", "", "", "2", "", "S", Constant.RIGORE_SEGNATO, "rigore_segnato"});
        eventi.put(10, new String[]{"autogol", "", "", "-3", "", "S", Constant.AUTOGOL, "autogol"});
        eventi.put(11, new String[]{"gol vittoria", "", "", "0", "", "N", Constant.GOL, "gol_decisivo_vittoria"});
        eventi.put(12, new String[]{"gol pareggio", "", "", "0", "", "N", Constant.GOL, "gol_decisivo_pareggio"});
        eventi.put(14, new String[]{"uscito", "", "", "0", "", "S", Constant.USCITO, ""});
        eventi.put(15, new String[]{"entrato", "", "", "0", "", "S", Constant.ENTRATO, ""});
        eventi.put(16, new String[]{"gol annullato", "", "", "0", "", "N", Constant.GOL_ANNULLATO, ""});
        eventi.put(17, new String[]{"infortunio", "", "", "0", "", "N", Constant.INFORTUNIO, ""});
        eventi.put(20, new String[]{"assist involontario", "", "", "1", "", "S", Constant.ASSIST, "assist_inv"});
        eventi.put(21, new String[]{"assist soft", "", "", "1", "", "S", Constant.ASSIST, "assist_soft"});
        eventi.put(22, new String[]{"assist", "", "", "1", "", "S", Constant.ASSIST, "assist"});
        eventi.put(23, new String[]{"assist_gold", "", "", "1", "", "S", Constant.ASSIST, "assist_gold"});
        eventi.put(24, new String[]{"assist movimento livello medio", "", "", "1", "", "S", Constant.ASSIST, "assistMovimentoLvMedio"});
        eventi.put(25, new String[]{"assist movimento livello alto", "", "", "1", "", "S", Constant.ASSIST, ""});
        eventi.put(26, new String[]{"mom", "", "", "1", "", "N", Constant.ASSIST, ""});

    }

    enum Dove {Casa, Trasferta}

    enum Ruolo {Titolari, Panchinari}

    private void go() throws Exception {

//        https://d2lhpso9w1g8dk.cloudfront.net/web/risorse/dati/live/20/live_3.json
        int g = 3;
        String sqFromLive = (String) Main.callHTTP("GET", "application/json; charset=UTF-8", "https://d2lhpso9w1g8dk.cloudfront.net/web/risorse/dati/live/" + Constant.I_LIVE_FANTACALCIO + "/live_" + g + ".json", null).get("response");
        Map<String, Object> jsonToMap = jsonToMap(sqFromLive);
        List<Map<String, Object>> getLiveFromFG = (List<Map<String, Object>>) ((Map) jsonToMap.get("data")).get("pl");
        List<Giocatore> lista = new ArrayList<>();

        for (int ii = 1; ii <= 4; ii++) {
            String testo = new String(Files.readAllBytes(Paths.get("C:\\1\\" + ii + ".html")));
            Document doc = Jsoup.parse(testo);
            for (Dove dove : Dove.values()) {
                for (Ruolo ruolo : Ruolo.values()) {
                    for (int i = 0; i < (ruolo == Ruolo.Titolari ? 11 : 14); i++) {
                        Giocatore giocatore = Main.estraiGiocatoreFromFS(doc, i, dove.toString(), ruolo.toString(), true);
                        lista.add(giocatore);
                    }
                }
            }
        }
        for (Giocatore giocatore : lista) {
            ricercaFSInLive(getLiveFromFG, giocatore);
        }
        stampa();
    }

    private void stampa() {

        printToken("Fantasquadra");
        printToken("Nome");
        printToken("Squadra");
        printToken("Ruolo");
        printToken("Voto");
        printToken("Modificatori");
        printToken("Id FS");
        printToken("Nome live");
        printToken("Squadra live");
        printToken("Voto live");
        printToken("BM Live");
        printToken("Id FG");
        System.out.println();

        mapGioc.forEach((giocatore, map) -> {
            printToken(giocatore.getFantasquadra());
            printToken(giocatore.getNome());
            printToken(giocatore.getSquadra());
            printToken(giocatore.getRuolo());
            printToken(giocatore.getVoto());
            printToken(giocatore.getModificatori());
            printToken(Long.valueOf(giocatore.getIdFs()) < 1000000
                    ? Long.valueOf(giocatore.getIdFs()) + 1000000
                    : giocatore.getIdFs());
            printToken(map.get("nome"));
            printToken(map.get("squadra"));
            printToken(map.get("voto"));
            printToken(map.get("bm"));
            printToken(map.get("id"));
            System.out.println();
        });
    }

    private void printToken(Object parola) {
        System.out.print("\"");
        System.out.print(parola.toString().replaceAll("\"", ""));
        System.out.print("\";");
    }

    private Map<String, Object> jsonToMap(String json) {
        try {
            ObjectMapper mapper;
            mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Map<Giocatore, Map<String, Object>> mapGioc = new LinkedHashMap<>();


    private void ricercaFSInLive(List<Map<String, Object>> getLiveFromFG, Giocatore giocatore) {
        String nomeGiocatoreFsCambiato = giocatore.getNomeFSCambiato();
        boolean trov = false;
        for (Map<String, Object> map : getLiveFromFG) {
            Map<String, Object> newMap = new HashMap<>();
            if (map.get("n").toString().equalsIgnoreCase(nomeGiocatoreFsCambiato)) {
                trov = true;
                Integer idS = (Integer) map.get("id_s");
                newMap.put("squadra", sq.get(idS));

                newMap.put("nome", nomeGiocatoreFsCambiato);
                String voto = "0";
                if (map.get("v") != null && !Double.valueOf(map.get("v").toString()).equals(56D)) {
                    voto = map.get("v").toString();
                }
                /*
        eventi.put(11, new String[]{"gol vittoria", "", "", "0", "", "N", Constant.GOL, "gol_decisivo_vittoria"});
        eventi.put(12, new String[]{"gol pareggio", "", "", "0", "", "N", Constant.GOL, "gol_decisivo_pareggio"});
        eventi.put(14, new String[]{"uscito", "", "", "0", "", "S", Constant.USCITO, ""});
        eventi.put(15, new String[]{"entrato", "", "", "0", "", "S", Constant.ENTRATO, ""});
        eventi.put(16, new String[]{"gol annullato", "", "", "0", "", "N", Constant.GOL_ANNULLATO, ""});
        eventi.put(17, new String[]{"infortunio", "", "", "0", "", "N", Constant.INFORTUNIO, ""});

                 */
                newMap.put("voto", voto);
                List<Integer> bm = (List) map.get("bm");
                String bbm = bm.stream()
                        .filter(b -> !b.equals(11) && !b.equals(12) && !b.equals(14) && !b.equals(15) && !b.equals(16) && !b.equals(17))
                        .map(b -> {
                            String[] val = eventi.getOrDefault(b, new String[]{"??"});
                            return "1 " + val[0] + "(" + (val[3].startsWith("-") ? "" : "+") + val[3] + ")";
                        })
                        .collect(Collectors.joining(", "));


                newMap.put("bm", "[" + bbm + "]");
                newMap.put("id", map.get("id"));
                mapGioc.put(giocatore, newMap);
            }
        }
        if (trov == false) {
            System.out.println(nomeGiocatoreFsCambiato + "-" + giocatore.getSquadra());
        }
    }

}
