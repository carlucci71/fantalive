package com.daniele.fantalive.model;

public class ConfigCampionato {
	Integer numGiocatori;
	String tipo; 
	String campionato;
	String tipologia;
	private String sfide;
	public ConfigCampionato(Integer numGiocatori, String tipo, String campionato, String tipologia, String sfide) {
		super();
		this.numGiocatori = numGiocatori;
		this.tipo = tipo;
		this.campionato = campionato;
		this.tipologia=tipologia;
		this.setSfide(sfide);
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
	public String getTipologia() {
		return tipologia;
	}
	public void setTipologia(String tipologia) {
		this.tipologia = tipologia;
	}
	public String getSfide() {
		return sfide;
	}
	public void setSfide(String sfide) {
		this.sfide = sfide;
	}
	@Override
	public String toString() {
		return "ConfigCampionato [numGiocatori=" + numGiocatori + ", tipo=" + tipo + ", campionato=" + campionato
				+ ", tipologia=" + tipologia + ", sfide=" + sfide + "]";
	}	
	
}
