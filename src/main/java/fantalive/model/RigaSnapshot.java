package fantalive.model;

public class RigaSnapshot {
	private String campionato;
	private String squadra;
	private String giocatore;
	public String getCampionato() {
		return campionato;
	}
	public void setCampionato(String campionato) {
		this.campionato = campionato;
	}
	public String getSquadra() {
		return squadra;
	}
	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}
	public String getGiocatore() {
		return giocatore;
	}
	public void setGiocatore(String giocatore) {
		this.giocatore = giocatore;
	}
	@Override
	public String toString() {
		return "RigaSnapshot [campionato=" + campionato + ", squadra=" + squadra + ", giocatore=" + giocatore + "]";
	}
	
}
