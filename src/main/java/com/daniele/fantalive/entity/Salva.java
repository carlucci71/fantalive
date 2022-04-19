package com.daniele.fantalive.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

@Entity
public class Salva {
	@Id
	private String nome;
	@Lob
	private String testo;

	public Salva() {
		super();
	}
	
	
	public Salva(String nome, String testo) {
		super();
		this.testo=testo;
		this.setNome(nome);
	}

	public String getTesto() {
		return testo;
	}

	public void setTesto(String testo) {
		this.testo = testo;
	}


	public String getNome() {
		return nome;
	}


	public void setNome(String nome) {
		this.nome = nome;
	}

	
}
