package com.daniele.fantalive.dto;

import java.util.Calendar;

public class GiocatoriPerSquadra {
	
	private String allenatore;
	private String squadra;
	private String ruolo;
	private String macroRuolo;
	private String giocatore;
	private int costo;
	private Calendar dataNascita;
	private Integer idGiocatore;
	private Integer idAllenatore;
	
	public GiocatoriPerSquadra(String allenatore, String squadra, String ruolo, String macroRuolo, String giocatore, int costo, Calendar dataNascita, Integer idGiocatore, Integer idAllenatore) {
		super();
		this.allenatore = allenatore;
		this.squadra = squadra;
		this.ruolo = ruolo;
		this.macroRuolo = macroRuolo;
		this.giocatore = giocatore;
		this.costo = costo;
		this.dataNascita=dataNascita;
		this.idGiocatore = idGiocatore;
		this.idAllenatore = idAllenatore;
	}
	public String getAllenatore() {
		return allenatore;
	}
	public void setAllenatore(String allenatore) {
		this.allenatore = allenatore;
	}
	public String getSquadra() {
		return squadra;
	}
	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}
	public String getRuolo() {
		return ruolo;
	}
	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}
	public String getGiocatore() {
		return giocatore;
	}
	public void setGiocatore(String giocatore) {
		this.giocatore = giocatore;
	}
	public int getCosto() {
		return costo;
	}
	public void setCosto(int costo) {
		this.costo = costo;
	}
	public String getMacroRuolo() {
		return macroRuolo;
	}
	public void setMacroRuolo(String macroRuolo) {
		this.macroRuolo = macroRuolo;
	}
	public Calendar getDataNascita() {
		return dataNascita;
	}
	public void setDataNascita(Calendar dataNascita) {
		this.dataNascita = dataNascita;
	}
	public Integer getIdGiocatore() {
		return idGiocatore;
	}
	public void setIdGiocatore(Integer idGiocatore) {
		this.idGiocatore = idGiocatore;
	}
	public Integer getIdAllenatore() {
		return idAllenatore;
	}
	public void setIdAllenatore(Integer idAllenatore) {
		this.idAllenatore = idAllenatore;
	}
	@Override
	public String toString() {
		return "GiocatoriPerSquadra [allenatore=" + allenatore + ", squadra=" + squadra + ", ruolo=" + ruolo
				+ ", macroRuolo=" + macroRuolo + ", giocatore=" + giocatore + ", costo=" + costo + ", dataNascita="
				+ dataNascita + ", idGiocatore=" + idGiocatore + ", idAllenatore=" + idAllenatore + "]";
	}

}
