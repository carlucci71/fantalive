package fantalive.model;

import java.util.Map;

public class Notifica implements Comparable<Notifica> {
	private String campionato;
	private String giocatore;
	private String squadra;
	private String id;
	private double voto=0;
	private Map<String,Integer> eventi;
	private String cambio="";
	public String getCambio() {
		return cambio;
	}
	public void setCambio(String cambio) {
		this.cambio = cambio;
	}
	public String getCampionato() {
		return campionato;
	}
	public void setCampionato(String campionato) {
		this.campionato = campionato;
	}
	public String getGiocatore() {
		return giocatore;
	}
	public void setGiocatore(String giocatore) {
		this.giocatore = giocatore;
	}
	public String getSquadra() {
		return squadra;
	}
	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	/*
	@Override
	public String toString() {
		String ret = giocatore + cambio + " " + id + " " + voto;
		Set<String> keySet = getEventi().keySet();
		for (String key : keySet) {
			if (getEventi().get(key)==null) {
				ret = ret + "\r\t\t\t "  + "  " + key;
			}
		}
		for (String key : keySet) {
			if (getEventi().get(key) != null && getEventi().get(key)>0) {
				ret = ret + "\r\t\t\t "  + getEventi().get(key) + " " + key;
			}
		}
		for (String key : keySet) {
			if (getEventi().get(key) != null && getEventi().get(key)<0) {
				ret = ret + "\r\t\t\t "  + (getEventi().get(key) * -1) + " --NO-- " + key;
			}
		}
		return ret;
	}
	*/
	public Map<String,Integer> getEventi() {
		return eventi;
	}
	public void setEventi(Map<String,Integer> eventi) {
		this.eventi = eventi;
	}
	public double getVoto() {
		return voto;
	}
	public void setVoto(double voto) {
		this.voto = voto;
	}
	@Override
	public int compareTo(Notifica o) {
		String thisId = this.getId().toUpperCase();
		String oId=o.getId().toUpperCase();
		if (thisId.startsWith("T") && oId.startsWith("R")) return -1;
		if (thisId.startsWith("R") && oId.startsWith("T")) return 1;
		return oId.compareTo(thisId);
	}

}
