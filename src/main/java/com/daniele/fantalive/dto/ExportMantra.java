package com.daniele.fantalive.dto;

public class ExportMantra {
	private String nome;
	private Integer idGiocatore;
	private Integer costo;
	public ExportMantra(String nome, Integer idGiocatore, Integer costo) {
		super();
		this.nome = nome;
		this.idGiocatore = idGiocatore;
		this.costo = costo;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Integer getIdGiocatore() {
		return idGiocatore;
	}
	public void setIdGiocatore(Integer idGiocatore) {
		this.idGiocatore = idGiocatore;
	}
	public Integer getCosto() {
		return costo;
	}
	public void setCosto(Integer costo) {
		this.costo = costo;
	}
	@Override
	public String toString() {
		return "ExportMantra [nome=" + nome + ", idGiocatore=" + idGiocatore + ", costo=" + costo + "]";
	}

}
