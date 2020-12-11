package fantalive.entity;

import java.util.ArrayList;
import java.util.List;

public class Squadra implements Comparable<Squadra> {
String nome;
private int deltaModificatore=0;
private boolean evidenza=false;
List<Giocatore> titolari=new ArrayList<Giocatore>();
private List<Giocatore> riserve=new ArrayList<Giocatore>();
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
	sb.append("" + getContaTitolari() + "con voto \n" + getContaSquadraTitolariNonGioca() + "ancora da giocare");
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
	sb.append("\n\n" + getProiezione());
	sb.append("\n\n" + getDeltaModificatore());
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
	return tot;
}
public int getContaTitolari() {
	int conta=0;
	for (Giocatore giocatore : titolari) {
		if (giocatore != null && giocatore.getVoto()>0) conta++;
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
public double getProiezione() {
	return getTotaleTitolari() + 6 * (11-getContaTitolari()) + getDeltaModificatore();
}
public boolean isEvidenza() {
	return evidenza;
}
public void setEvidenza(boolean evidenza) {
	this.evidenza = evidenza;
}
@Override
public int compareTo(Squadra o) {
	if (this.getNome()==null) return -1;
	if (o.getNome()==null) return 1;
	return this.getNome().toUpperCase().compareTo(o.getNome().toUpperCase());
}
public int getDeltaModificatore() {
	return deltaModificatore;
}
public void setDeltaModificatore(int deltaModificatore) {
	this.deltaModificatore = deltaModificatore;
}

}
