package com.daniele.fantalive.model;

import java.util.ArrayList;
import java.util.List;

public class Rosa{
String nome;
private String lega;
List<Giocatore> giocatori=new ArrayList<Giocatore>();
public String getNome() {
	return nome;
}
public void setNome(String nome) {
	this.nome = nome;
}
public List<Giocatore> getGiocatori() {
	return giocatori;
}
public void setGiocatori(List<Giocatore> giocatori) {
	this.giocatori = giocatori;
}
@Override
public String toString() {
	return nome + " " + lega + "\ngiocatori=" + giocatori + "\n\n";
}
public String getLega() {
	return lega;
}
public void setLega(String lega) {
	this.lega = lega;
}

}
