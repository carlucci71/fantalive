package com.daniele.fantalive.model;

import java.util.List;
import java.util.Map;

public class Live {
	private String squadra;
	private List<Map<String, Object>> giocatori;
	public String getSquadra() {
		return squadra;
	}
	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}
	public List<Map<String, Object>> getGiocatori() {
		return giocatori;
	}
	public void setGiocatori(List<Map<String, Object>> giocatori) {
		this.giocatori = giocatori;
	}
	@Override
	public String toString() {
		return "Live [squadra=" + squadra + ", giocatori=" + giocatori + "]";
	}
}
