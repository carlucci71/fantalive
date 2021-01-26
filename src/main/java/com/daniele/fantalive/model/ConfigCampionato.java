package com.daniele.fantalive.model;

public class ConfigCampionato {
	Integer numGiocatori;
	String tipo; 
	String campionato;
	public ConfigCampionato(Integer numGiocatori, String tipo, String campionato) {
		super();
		this.numGiocatori = numGiocatori;
		this.tipo = tipo;
		this.campionato = campionato;
	}
	@Override
	public String toString() {
		return "ConfigCampionato [numGiocatori=" + numGiocatori + ", tipo=" + tipo + ", campionato=" + campionato + "]";
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
	
}
