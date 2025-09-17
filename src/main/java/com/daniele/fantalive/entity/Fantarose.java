package com.daniele.fantalive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
//@Table(name = "fantarose")
public class Fantarose {

	@Id
	@Column(name = "idgiocatore")
	private Integer idGiocatore;
	@Column(name = "idallenatore")
	private Integer idAllenatore;
	private Integer costo;
	@Column(name = "sqltime")
	private String  sqlTime;
	@Override
	public String toString() {
		return "Fantarose [idGiocatore=" + idGiocatore + ", idAllenatore=" + idAllenatore + ", costo=" + costo
				+ ", sqlTime=" + sqlTime + "]";
	}
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
	public Integer getCosto() {
		return costo;
	}
	public void setCosto(Integer costo) {
		this.costo = costo;
	}
	public String getSqlTime() {
		return sqlTime;
	}
	public void setSqlTime(String sqlTime) {
		this.sqlTime = sqlTime;
	}
}
