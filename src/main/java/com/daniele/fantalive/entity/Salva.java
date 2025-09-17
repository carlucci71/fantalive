package com.daniele.fantalive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class Salva {
	@Id
	private String nome;
	@Lob
	@Column(columnDefinition = "text")
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


	@Override
	public String toString() {
		return "Salva [nome=(" + nome.length() + ") " + nome + ", testo=(" + testo.length() + ") " + testo + "]";
	}

	
}
