package com.daniele.asta.support;

import com.daniele.fantalive.entity.Allenatori;
import com.daniele.fantalive.entity.Configurazione;
import com.daniele.fantalive.entity.Giocatori;
import com.daniele.fantalive.repository.AllenatoriRepository;
import com.daniele.fantalive.repository.ConfigurazioneRepository;
import com.daniele.fantalive.repository.FantaroseRepository;
import com.daniele.fantalive.repository.GiocatoriRepository;
import com.daniele.fantalive.repository.LoggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AstaTestFixture {

    public static final String PLAYER_1 = "TEST_ALFA";
    public static final String PLAYER_2 = "TEST_BETA";
    public static final String PLAYER_3 = "TEST_GAMMA";

    @Autowired
    private ConfigurazioneRepository configurazioneRepository;
    @Autowired
    private AllenatoriRepository allenatoriRepository;
    @Autowired
    private GiocatoriRepository giocatoriRepository;
    @Autowired
    private FantaroseRepository fantaroseRepository;
    @Autowired
    private LoggerRepository loggerRepository;

    public void resetAndSeed() {
        loggerRepository.deleteAll();
        fantaroseRepository.deleteAll();
        giocatoriRepository.deleteAll();
        allenatoriRepository.deleteAll();
        configurazioneRepository.deleteAll();

        Configurazione cfg = new Configurazione();
        cfg.setId(0);
        cfg.setNumeroGiocatori(3);
        cfg.setBudget(500);
        cfg.setDurataAsta(30);
        cfg.setNumeroAcquisti(25);
        cfg.setNumeroMinAcquisti(0);
        cfg.setMaxP(3);
        cfg.setMaxD(8);
        cfg.setMaxC(8);
        cfg.setMaxA(6);
        cfg.setMinP(0);
        cfg.setMinD(0);
        cfg.setMinC(0);
        cfg.setMinA(0);
        cfg.setIsATurni(false);
        cfg.setIsSingle(false);
        cfg.setMantra(false);
        configurazioneRepository.save(cfg);

        for (int i = 0; i < 3; i++) {
            Allenatori al = new Allenatori();
            al.setId(i);
            al.setOrdine(i);
            al.setNome("GIOC" + i);
            al.setPwd("");
            al.setIsAdmin(i == 0);
            allenatoriRepository.save(al);
        }

        savePlayer(1, PLAYER_1, "A", "AAA");
        savePlayer(2, PLAYER_2, "A", "BBB");
        savePlayer(3, PLAYER_3, "D", "CCC");
    }

    private void savePlayer(int id, String nome, String ruolo, String squadra) {
        Giocatori g = new Giocatori();
        g.setId(id);
        g.setNome(nome);
        g.setRuolo(ruolo);
        g.setMacroRuolo(ruolo);
        g.setSquadra(squadra);
        g.setQuotazione(10);
        giocatoriRepository.save(g);
    }
}
