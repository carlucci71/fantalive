package com.daniele.fantalive.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(GiocatoriFavoritiId.class)
public class GiocatoriFavoriti implements Serializable{

	@Id
	@Column(name = "idgiocatore")
	private Integer idGiocatore;
	@Id
	@Column(name = "idallenatore")
	private Integer idAllenatore;
	private String nota;
	public Integer getIdGiocatore() {
		return idGiocatore;
	}
	public void setIdGiocatore(Integer idGiocatore) {
		this.idGiocatore = idGiocatore;
	}
	public Integer getIdAllenatore() {
		return idAllenatore;
	}
	public void setIdAllenatore(Integer idAllenatore) {
		this.idAllenatore = idAllenatore;
	}
	public String getNota() {
		return nota;
	}
	public void setNota(String nota) {
		this.nota = nota;
	}
	@Override
	public String toString() {
		return "GiocatoriFavoriti [idGiocatore=" + idGiocatore + ", idAllenatore=" + idAllenatore + ", nota=" + nota
				+ "]";
	}

}
