package com.daniele.fantalive.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class Squadra implements Comparable<Squadra> {
public Squadra() {
		super();
	}
String nome;
private Boolean casaProiezione=false;
private int prog=0;
private Boolean evidenza=false;
private double modificatoreDifesa=0;
private double modificatoreCentrocampo=0;
private double modificatoreDifesaDaAssegnare=0;
private double modificatoreAttacco=0;
private double malusFormazioneAutomatica=0;
private List<Giocatore> titolari=new ArrayList<Giocatore>();
private List<Giocatore> titolariOriginali=new ArrayList<Giocatore>();
private List<Giocatore> riserveOriginali=new ArrayList<Giocatore>();
private List<PartitaSimulata> partiteSimulate=new ArrayList<PartitaSimulata>();
private List<Giocatore> riserve=new ArrayList<Giocatore>();
private String idSquadra;
private String modulo;
private String nick=null;
private double fairPlay=0;

public String getNome() {
	return nome;
}
public void setNome(String nome) {
	this.nome = nome;
}
public List<Giocatore> getTitolari() {
	return titolari;
}
public void setTitolari(List<Giocatore> titolari) {
	this.titolari = titolari;
}
@Override
public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(nome + (nick==null?"":" (" + nick + ") ") + "\nTITOLARI:\n");
	for (Giocatore giocatore : titolari) {
		sb.append(giocatore + "\n");
	}
	sb.append("\n" + getTotaleTitolari() + " tot\n");
	sb.append("" + getContaTitolari() + " con voto \n" + getContaSquadraTitolariNonGioca() + " ancora da giocare");
	sb.append("\n" + getTotaleTitolari()/getContaTitolari());
	sb.append(" media votati");
	sb.append("\n\nRISERVE:\n");
	for (Giocatore giocatore : riserve) {
		sb.append(giocatore + "\n");
	}
	sb.append("\n" + getTotaleRiserve() + " tot\n");
	sb.append("" + getContaRiserve() + " con voto \n" + getContaSquadraRiserveNonGioca() + " ancora da giocare");
	sb.append("\n" + getTotaleRiserve()/getContaRiserve());
	sb.append(" media votati");
	sb.append("\n\n Proiezione: " + getProiezione());
	sb.append("\n\n Modificatore Difesa Da Assegnare: " + getModificatoreDifesaDaAssegnare());
	sb.append("\n\n Modificatore Difesa: " + getModificatoreDifesa());
	sb.append("\n\n Modificatore Centrocampo: " + getModificatoreCentrocampo());
	sb.append("\n\n Modificatore Attacco: " + getModificatoreAttacco());
	sb.append("\n\n Malus Formazione Automatica: " + getMalusFormazioneAutomatica());
	sb.append("\n\n TOTALE: " + getTotale());
	sb.append("\nTITOLARI ORIGINALI:\n");
	for (Giocatore giocatore : getTitolariOriginali()) {
		sb.append(giocatore + "\n");
	}
	return sb.toString();
}
public List<Giocatore> getRiserve() {
	return riserve;
}
public void setRiserve(List<Giocatore> riserve) {
	this.riserve = riserve;
}
public double getTotaleTitolari() {
	double tot=0;
	for (Giocatore giocatore : titolari) {
		if (giocatore != null) {
			tot=tot +  giocatore.getModificatore() + giocatore.getVoto();
		}
	}
	return new BigDecimal(tot, MathContext.DECIMAL128).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
}
public int getContaTitolari() {
	int conta=0;
	for (Giocatore giocatore : titolari) {
		if (giocatore != null && giocatore.getVoto()>0) conta++;
	}
	return conta;
}
public int getContaNonCambiabili() {
	int conta=0;
	for (Giocatore giocatore : titolari) {
		if (giocatore != null && giocatore.isNonCambiabile()) conta++;
	}
	return conta;
}
public int getContaSquadraTitolariNonGioca() {
	int conta=0;
	for (Giocatore giocatore : titolari) {
		if (giocatore != null && !giocatore.isSquadraGioca()) conta++;
	}
	return conta;
}
public int getContaSquadraRiserveNonGioca() {
	int conta=0;
	for (Giocatore giocatore : riserve) {
		if (giocatore != null && !giocatore.isSquadraGioca()) conta++;
	}
	return conta;
}
public double getMediaTitolari() {
	return Math.ceil(getTotaleTitolari()/getContaTitolari()*100)/100;
}
public double getTotaleRiserve() {
	double tot=0;
	for (Giocatore giocatore : riserve) {
		if (giocatore != null) {
			tot=tot +  giocatore.getModificatore() + giocatore.getVoto();
		}
	}
	return tot;
}
public int getContaRiserve() {
	int conta=0;
	for (Giocatore giocatore : riserve) {
		if (giocatore != null && giocatore.getVoto()>0) conta++;
	}
	return conta;
}
public double getMediaRiserve() {
	return Math.ceil(getTotaleRiserve()/getContaRiserve()*100)/100;
}
public Double getProiezione() {
	double d = getTotaleTitolari() + 6 * (11-getContaTitolari() - getContaNonCambiabili());
	for (Giocatore giocatore : titolari) {
		if (giocatore.getMantraMalus() != null) {
			d=d+giocatore.getMantraMalus();
		}
	}
	d=d+fairPlay;
	return new BigDecimal(d, MathContext.DECIMAL128).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
}
public Double getTotale() {
//	return getTotaleTitolari() + getModificatoreDifesa() + getModificatoreCentrocampo() + getModificatoreAttacco() + getMalusFormazioneAutomatica();
	
	double d = getTotaleTitolari() + getModificatoreDifesa() + getModificatoreCentrocampo() + getModificatoreAttacco() + getMalusFormazioneAutomatica();
	return new BigDecimal(d, MathContext.DECIMAL128).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
}
public Boolean isEvidenza() {
	return evidenza;
}
public void setEvidenza(Boolean evidenza) {
	this.evidenza = evidenza;
}
@Override
public int compareTo(Squadra o) {
	if (this.getProiezione()==null) return -1;
	if (o.getProiezione()==null) return 1;
	try {
		Double proiezioneThis = this.getProiezione();
		Double proiezioneO = o.getProiezione();
		if (this.isCasaProiezione()) {
			proiezioneThis=proiezioneThis+2;
		}
		if (o.isCasaProiezione()){
			proiezioneO=proiezioneO+2;
		}
		return proiezioneO.compareTo(proiezioneThis);
	}
	catch (Exception e)
	{
		if (this.getNome()==null) return -1;
		if (o.getNome()==null) return 1;
		return this.getNome().toUpperCase().compareTo(o.getNome().toUpperCase());
	}
}
public int getProg() {
	return prog;
}
public void setProg(int prog) {
	this.prog = prog;
}
public List<PartitaSimulata> getPartiteSimulate() {
	return partiteSimulate;
}
public void setPartiteSimulate(List<PartitaSimulata> partiteSimulate) {
	this.partiteSimulate = partiteSimulate;
}
public Boolean isCasaProiezione() {
	if (casaProiezione==null) return false;
	return casaProiezione;
}
public void setCasaProiezione(Boolean casaProiezione) {
	this.casaProiezione = casaProiezione;
}
public double getModificatoreDifesa() {
	return modificatoreDifesa;
}
public void setModificatoreDifesa(double modificatoreDifesa) {
	this.modificatoreDifesa = modificatoreDifesa;
}
public double getModificatoreDifesaDaAssegnare() {
	return modificatoreDifesaDaAssegnare;
}
public void setModificatoreDifesaDaAssegnare(double modificatoreDifesaDaAssegnare) {
	this.modificatoreDifesaDaAssegnare = modificatoreDifesaDaAssegnare;
}
public double getModificatoreAttacco() {
	return modificatoreAttacco;
}
public void setModificatoreAttacco(double modificatoreAttacco) {
	this.modificatoreAttacco = modificatoreAttacco;
}
public double getMalusFormazioneAutomatica() {
	return malusFormazioneAutomatica;
}
public void setMalusFormazioneAutomatica(double malusFormazioneAutomatica) {
	this.malusFormazioneAutomatica = malusFormazioneAutomatica;
}
public double getModificatoreCentrocampo() {
	return modificatoreCentrocampo;
}
public void setModificatoreCentrocampo(double modificatoreCentrocampo) {
	this.modificatoreCentrocampo = modificatoreCentrocampo;
}
public List<Giocatore> getTitolariOriginali() {
	return titolariOriginali;
}
public void setTitolariOriginali(List<Giocatore> titolariOriginali) {
	this.titolariOriginali = titolariOriginali;
}
public String getIdSquadra() {
	return idSquadra;
}
public void setIdSquadra(String idSquadra) {
	this.idSquadra = idSquadra;
}
public String getModulo() {
	return modulo;
}
public void setModulo(String modulo) {
	this.modulo = modulo;
}
public List<Giocatore> getRiserveOriginali() {
	return riserveOriginali;
}
public void setRiserveOriginali(List<Giocatore> riserveOriginali) {
	this.riserveOriginali = riserveOriginali;
}
public double getFairPlay() {
	return fairPlay;
}
public void setFairPlay(double fairPlay) {
	this.fairPlay = fairPlay;
}
public String getNick() {
	return nick;
}
public void setNick(String nick) {
	this.nick = nick;
}
public Squadra clonaSquadra () throws Exception {
	Squadra squadra = new Squadra();
	Field[] fields = this.getClass().getDeclaredFields();
	for (Field field : fields) {
		System.out.println(field.getName());
		Method methodGet = squadra.getClass().getMethod((field.getType().equals(Boolean.class)?"is":"get") + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1));
		Method methodPut = squadra.getClass().getMethod("set" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1), field.getType());
//		System.out.println(methodGet.invoke(this));
//		System.out.println(methodPut.getName());
		methodPut.invoke(squadra, methodGet.invoke(this));
	}
	/*
	squadra.setModificatoreDifesa(getModificatoreDifesa());
	squadra.setModificatoreDifesaDaAssegnare(getModificatoreDifesaDaAssegnare());
	squadra.setModificatoreCentrocampo(getModificatoreCentrocampo());
	squadra.setModificatoreAttacco(getModificatoreAttacco());
	squadra.setMalusFormazioneAutomatica(getMalusFormazioneAutomatica());
	squadra.setFairPlay(getFairPlay());
	squadra.setCasaProiezione(isCasaProiezione());
	squadra.setModulo((getModulo()));
	squadra.setIdSquadra(getIdSquadra());
	squadra.setTitolariOriginali(getTitolariOriginali());
	squadra.setRiserveOriginali(getRiserveOriginali());
	squadra.setNome(getNome());
	squadra.setEvidenza(isEvidenza());
	squadra.setPartiteSimulate(getPartiteSimulate());
	squadra.setProg(getProg());
	squadra.setNick(getNick());
	squadra.setTitolari(getTitolari());
	squadra.setRiserve(getRiserve());
	*/
	return squadra;
}


}
