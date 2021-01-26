package com.daniele.fantalive.dto;

public class SpesoTotale {

	public SpesoTotale(String nome, String macroRuolo, Long costo, Long conta) {
		super();
		this.nome = nome;
		this.macroRuolo = macroRuolo;
		this.costo = costo;
		this.conta = conta;
	}
	private String nome;
	private String macroRuolo;
	private Long costo;
	private Long conta;
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getMacroRuolo() {
		return macroRuolo;
	}
	public void setMacroRuolo(String macroRuolo) {
		this.macroRuolo = macroRuolo;
	}
	public Long getCosto() {
		return costo;
	}
	public void setCosto(Long costo) {
		this.costo = costo;
	}
	public Long getConta() {
		return conta;
	}
	public void setConta(Long conta) {
		this.conta = conta;
	}
	@Override
	public String toString() {
		return "SpesoTotale [nome=" + nome + ", macroRuolo=" + macroRuolo + ", costo=" + costo + ", conta=" + conta
				+ "]";
	}
	

	
}