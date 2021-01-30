package com.daniele.fantalive.entity;

import java.io.Serializable;

public class GiocatoriFavoritiId implements Serializable{

	private Integer idGiocatore;
	private Integer idAllenatore;
	@Override
	public String toString() {
		return "GiocatoriFavoritiId [idGiocatore=" + idGiocatore + ", idAllenatore=" + idAllenatore + "]";
	}

	
}
