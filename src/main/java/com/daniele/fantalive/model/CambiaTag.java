package com.daniele.fantalive.model;

public class CambiaTag {
	private String Squadra;
	private String Tag;
	public String getSquadra() {
		return Squadra;
	}
	public void setSquadra(String squadra) {
		Squadra = squadra;
	}
	public String getTag() {
		return Tag;
	}
	public void setTag(String tag) {
		Tag = tag;
	}
	@Override
	public String toString() {
		return "CambiaTag [Squadra=" + Squadra + ", Tag=" + Tag + "]";
	}

}
