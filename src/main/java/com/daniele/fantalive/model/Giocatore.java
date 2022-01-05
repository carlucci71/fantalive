package com.daniele.fantalive.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Giocatore {
	private int numGol=0;
	private List<Integer> codEventi=new ArrayList<Integer>();
	private List<String> modificatori=new ArrayList<String>();
	private Map<String,String> orario=new HashMap<String,String>();
	private String id;
	private String ruolo;
	private String nome;
	private String nomeTrim;
	private String nomeLive;
	private String ruoloLive;
	private double voto=0;
	private boolean squadraGioca;
	private String evento="";
	private double modificatore=0;
	private String squadra;
	private boolean cambio;
	private boolean cambiato;
	private boolean nonCambiabile;
	private boolean mantraCambio;
	private Double mantraMalus;
	private String idGioc;
	private boolean entra=false;
	private boolean esce=false;
	private boolean notificaLive;
	public String getRuolo() {
		return ruolo;
	}
	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getSquadra() {
		return squadra;
	}
	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}
	public String getNomeTrim() {
		return nomeTrim;
	}
	public void setNomeTrim(String nomeTrim) {
		this.nomeTrim = nomeTrim;
	}
	public String getNomeLive() {
		return nomeLive;
	}
	public void setNomeLive(String nomeLive) {
		this.nomeLive = nomeLive;
	}
	public String getRuoloLive() {
		return ruoloLive;
	}
	public void setRuoloLive(String ruoloLive) {
		this.ruoloLive = ruoloLive;
	}
	public double getVoto() {
		return voto;
	}
	public void setVoto(double voto) {
		this.voto = voto;
	}
	public String getEvento() {
		return evento;
	}
	public void setEvento(String evento) {
		this.evento = evento;
	}
	public double getModificatore() {
		return modificatore;
	}
	public void setModificatore(double modificatore) {
		this.modificatore = modificatore;
	}
	@Override
	public String toString() {
		return nome + "(" + ruolo + ") " + squadra + " " + id + " " + (squadraGioca?"":"N/A") + " " + evento + codEventi   
				+ " voto=" + voto  
				+ " numGol=" + numGol  
				+ " entra=" + entra  
				+ " esce=" + esce
				+ " modificatori=" + modificatori  
				+ " modificatore=" + modificatore + ", FM=" + (modificatore + voto)
				+ " orario=" + orario + " cambio=" + cambio + " idGioc=" + idGioc; 
	}
	public boolean isSquadraGioca() {
		return squadraGioca;
	}
	public void setSquadraGioca(boolean squadraGioca) {
		this.squadraGioca = squadraGioca;
	}
	public List<Integer> getCodEventi() {
		return codEventi;
	}
	public void setCodEventi(List<Integer> codEventi) {
		this.codEventi = codEventi;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Map<String,String> getOrario() {
		return orario;
	}
	public void setOrario(Map<String,String> orario) {
		this.orario = orario;
	}
	public boolean isCambio() {
		return cambio;
	}
	public void setCambio(boolean cambio) {
		this.cambio = cambio;
	}
	public boolean isCambiato() {
		return cambiato;
	}
	public void setCambiato(boolean cambiato) {
		this.cambiato = cambiato;
	}
	public String getIdGioc() {
		return idGioc;
	}
	public void setIdGioc(String idGioc) {
		this.idGioc = idGioc;
	}
	public boolean isNotificaLive() {
		return notificaLive;
	}
	public void setNotificaLive(boolean notificaLive) {
		this.notificaLive = notificaLive;
	}
	public boolean isNonCambiabile() {
		return nonCambiabile;
	}
	public void setNonCambiabile(boolean nonCambiabile) {
		this.nonCambiabile = nonCambiabile;
	}
	public List<String> getModificatori() {
		return modificatori;
	}
	public void setModificatori(List<String> modificatori) {
		this.modificatori = modificatori;
	}
	public int getNumGol() {
		return numGol;
	}
	public void setNumGol(int numGol) {
		this.numGol = numGol;
	}
	public boolean isEntra() {
		return entra;
	}
	public void setEntra(boolean entra) {
		this.entra = entra;
	}
	public boolean isEsce() {
		return esce;
	}
	public void setEsce(boolean esce) {
		this.esce = esce;
	}
	public boolean isMantraCambio() {
		return mantraCambio;
	}
	public void setMantraCambio(boolean mantraCambio) {
		this.mantraCambio = mantraCambio;
	}
	public Double getMantraMalus() {
		return mantraMalus;
	}
	public void setMantraMalus(Double mantraMalus) {
		this.mantraMalus = mantraMalus;
	}

}
