package com.daniele.fantalive.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class Squadra implements Comparable<Squadra> {
String nome;
private boolean casaProiezione;
private int prog=0;
private boolean evidenza=false;
private double modificatoreDifesa=0;
private double modificatoreCentrocampo=0;
private double modificatoreDifesaDaAssegnare=0;
private double modificatoreAttacco=0;
private double malusFormazioneAutomatica=0;
private List<Giocatore> titolari=new ArrayList<Giocatore>();
private List<Giocatore> titolariOriginali=new ArrayList<Giocatore>();
private List<PartitaSimulata> partiteSimulate=new ArrayList<PartitaSimulata>();
private List<Giocatore> riserve=new ArrayList<Giocatore>();
public double tmp;

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
	sb.append(nome + "\nTITOLARI:\n");
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
	return getTotaleTitolari() + 6 * (11-getContaTitolari() - getContaNonCambiabili()) ;
}
public Double getTotale() {
//	return getTotaleTitolari() + getModificatoreDifesa() + getModificatoreCentrocampo() + getModificatoreAttacco() + getMalusFormazioneAutomatica();
	
	double d = getTotaleTitolari() + getModificatoreDifesa() + getModificatoreCentrocampo() + getModificatoreAttacco() + getMalusFormazioneAutomatica();
	return new BigDecimal(d, MathContext.DECIMAL128).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
}
public boolean isEvidenza() {
	return evidenza;
}
public void setEvidenza(boolean evidenza) {
	this.evidenza = evidenza;
}
@Override
public int compareTo(Squadra o) {
//	if (this.getProiezione()==null) return -1;
//	if (o.getProiezione()==null) return 1;
	Double proiezioneThis = this.getProiezione();
	Double proiezioneO = o.getProiezione();
	if (this.isCasaProiezione()) {
		proiezioneThis=proiezioneThis+2;
	}
	if (o.isCasaProiezione()){
		proiezioneO=proiezioneO+2;
	}
	return proiezioneO.compareTo(proiezioneThis);
	/*
	if (this.getNome()==null) return -1;
	if (o.getNome()==null) return 1;
	return this.getNome().toUpperCase().compareTo(o.getNome().toUpperCase());
	*/
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
public boolean isCasaProiezione() {
	return casaProiezione;
}
public void setCasaProiezione(boolean casaProiezione) {
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

}
