package com.daniele.fantalive.entity;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.Id;

//@Entity(name = "giocatori")
//@Table(name = "giocatori")
@Entity
public class Giocatori {

	@Id
	private Integer id;
	private String squadra;
	private String nome;
	private String ruolo;
	private String macroRuolo;
	private Integer quotazione;
	private Calendar dataNascita;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getSquadra() {
		return squadra;
	}
	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getRuolo() {
		return ruolo;
	}
	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}
	public Integer getQuotazione() {
		return quotazione;
	}
	public void setQuotazione(Integer quotazione) {
		this.quotazione = quotazione;
	}
	public String getMacroRuolo() {
		return macroRuolo;
	}
	public void setMacroRuolo(String macroRuolo) {
		this.macroRuolo = macroRuolo;
	}
	public Calendar getDataNascita() {
		return dataNascita;
	}
	public void setDataNascita(Calendar dataNascita) {
		this.dataNascita = dataNascita;
	}
	
	
	@Override
	public String toString() {
		return "Giocatori [id=" + id + ", squadra=" + squadra + ", nome=" + nome + ", ruolo=" + ruolo + ", macroRuolo="
				+ macroRuolo + ", quotazione=" + quotazione + ", dataNascita=" + dataNascita + "]";
	}
}
