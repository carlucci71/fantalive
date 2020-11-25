package scrap;

import java.util.ArrayList;
import java.util.List;

public class Return {

	private String nome;
	private String campionato;
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
	@Override
	public String toString() {
		return "Return [nome=" + nome + ", campionato=" + campionato + ", squadre=" + squadre + "]";
	}
	
}
