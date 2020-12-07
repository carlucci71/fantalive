package scrap;

import java.util.List;

public class Notifica {
	private String campionato;
	private String giocatore;
	private String squadra;
	private String id;
	private double voto=0;
	private List<String> eventi;
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
	@Override
	public String toString() {
		return giocatore + cambio + " " + id + " " + getEventi() + " " + voto;
	}
	public List<String> getEventi() {
		return eventi;
	}
	public void setEventi(List<String> eventi) {
		this.eventi = eventi;
	}
	public double getVoto() {
		return voto;
	}
	public void setVoto(double voto) {
		this.voto = voto;
	}

}
