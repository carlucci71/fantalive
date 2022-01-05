package com.daniele.fantalive.model;

public class ConfigCampionato {
	Integer numGiocatori;
	String tipo; 
	String campionato;
	String tipologia;
	public ConfigCampionato(Integer numGiocatori, String tipo, String campionato, String tipologia) {
		super();
		this.numGiocatori = numGiocatori;
		this.tipo = tipo;
		this.campionato = campionato;
		this.tipologia=tipologia;
	}
	public Integer getNumGiocatori() {
		return numGiocatori;
	}
	public void setNumGiocatori(Integer numGiocatori) {
		this.numGiocatori = numGiocatori;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getCampionato() {
		return campionato;
	}
	public void setCampionato(String campionato) {
		this.campionato = campionato;
	}
	@Override
	public String toString() {
		return "ConfigCampionato [numGiocatori=" + numGiocatori + ", tipo=" + tipo + ", campionato=" + campionato
				+ ", tipologia=" + tipologia + "]";
	}
	public String getTipologia() {
		return tipologia;
	}
	public void setTipologia(String tipologia) {
		this.tipologia = tipologia;
	}	
	
}
