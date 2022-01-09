package com.daniele.fantalive.model;

import java.util.ArrayList;
import java.util.List;

public class Return {
	private String sfide;
	private String tipologia;
	private String tipo;
	private String aggiornamento;
	private String nome;
	private String campionato;
	private boolean conLive;
	private List<Squadra> squadre=new ArrayList<Squadra>();
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public List<Squadra> getSquadre() {
		return squadre;
	}
	public void setSquadre(List<Squadra> squadre) {
		this.squadre = squadre;
	}
	public String getCampionato() {
		return campionato;
	}
	public void setCampionato(String campionato) {
		this.campionato = campionato;
	}
	public boolean isConLive() {
		return conLive;
	}
	public void setConLive(boolean conLive) {
		this.conLive = conLive;
	}
	public String getAggiornamento() {
		return aggiornamento;
	}
	public void setAggiornamento(String aggiornamento) {
		this.aggiornamento = aggiornamento;
	}
	public String getTipologia() {
		return tipologia;
	}
	public void setTipologia(String tipologia) {
		this.tipologia = tipologia;
	}

	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getSfide() {
		return sfide;
	}
	public void setSfide(String sfide) {
		this.sfide = sfide;
	}
	@Override
	public String toString() {
		return "Return [sfide=" + sfide + ", tipologia=" + tipologia + ", tipo=" + tipo + ", aggiornamento="
				+ aggiornamento + ", nome=" + nome + ", campionato=" + campionato + ", conLive=" + conLive
				+ ", squadre=" + squadre + "]";
	}
	
}
