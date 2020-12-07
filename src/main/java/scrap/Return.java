package scrap;

import java.util.ArrayList;
import java.util.List;

public class Return {

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
	@Override
	public String toString() {
		return "Return [aggiornamento=" + aggiornamento + ", nome=" + nome + ", campionato=" + campionato + ", conLive="
				+ conLive + ", squadre=" + squadre + "]";
	}
	
}
