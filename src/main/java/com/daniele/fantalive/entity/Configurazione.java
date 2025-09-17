package com.daniele.fantalive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Configurazione {

	@Id
	private Integer id;
	private Boolean isATurni;
	private Boolean isSingle;
	private boolean isMantra;
	private Integer budget;
	private Integer durataAsta;
	private Integer numeroAcquisti;
	private Integer numeroMinAcquisti;
	
	private Integer maxP;
	private Integer maxD;
	private Integer maxC;
	private Integer maxA;

	private Integer minP;
	private Integer minD;
	private Integer minC;
	private Integer minA;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}


	public Integer getNumeroGiocatori() {
		return numeroGiocatori;
	}
	public void setNumeroGiocatori(Integer numeroGiocatori) {
		this.numeroGiocatori = numeroGiocatori;
	}


	public Boolean getIsATurni() {
		return isATurni;
	}
	public void setIsATurni(Boolean isATurni) {
		this.isATurni = isATurni;
	}

	@Column(nullable = true)
	private Integer numeroGiocatori;
	public Integer getBudget() {
		return budget;
	}
	public void setBudget(Integer budget) {
		this.budget = budget;
	}
	public Integer getNumeroAcquisti() {
		return numeroAcquisti;
	}
	public void setNumeroAcquisti(Integer numeroAcquisti) {
		this.numeroAcquisti = numeroAcquisti;
	}
	public boolean isMantra() {
		return isMantra;
	}
	public void setMantra(boolean isMantra) {
		this.isMantra = isMantra;
	}

	public Integer getMaxP() {
		return maxP;
	}
	public void setMaxP(Integer maxP) {
		this.maxP = maxP;
	}
	public Integer getMaxD() {
		return maxD;
	}
	public void setMaxD(Integer maxD) {
		this.maxD = maxD;
	}
	public Integer getMaxC() {
		return maxC;
	}
	public void setMaxC(Integer maxC) {
		this.maxC = maxC;
	}
	public Integer getMaxA() {
		return maxA;
	}
	public void setMaxA(Integer maxA) {
		this.maxA = maxA;
	}
	public Integer getNumeroMinAcquisti() {
		return numeroMinAcquisti;
	}
	public void setNumeroMinAcquisti(Integer numeroMinAcquisti) {
		this.numeroMinAcquisti = numeroMinAcquisti;
	}

	public Integer getDurataAsta() {
		return durataAsta;
	}
	public void setDurataAsta(Integer durataAsta) {
		this.durataAsta = durataAsta;
	}
	public Integer getMinP() {
		return minP;
	}
	public void setMinP(Integer minP) {
		this.minP = minP;
	}
	public Integer getMinD() {
		return minD;
	}
	public void setMinD(Integer minD) {
		this.minD = minD;
	}
	public Integer getMinC() {
		return minC;
	}
	public void setMinC(Integer minC) {
		this.minC = minC;
	}
	public Integer getMinA() {
		return minA;
	}
	public void setMinA(Integer minA) {
		this.minA = minA;
	}
	public Boolean getIsSingle() {
		return isSingle;
	}
	public void setIsSingle(Boolean isSingle) {
		this.isSingle = isSingle;
	}
	@Override
	public String toString() {
		return "Configurazione [id=" + id + ", isATurni=" + isATurni + ", isSingle=" + isSingle + ", isMantra="
				+ isMantra + ", budget=" + budget + ", durataAsta=" + durataAsta + ", numeroAcquisti=" + numeroAcquisti
				+ ", numeroMinAcquisti=" + numeroMinAcquisti + ", maxP=" + maxP + ", maxD=" + maxD + ", maxC=" + maxC
				+ ", maxA=" + maxA + ", minP=" + minP + ", minD=" + minD + ", minC=" + minC + ", minA=" + minA
				+ ", numeroGiocatori=" + numeroGiocatori + "]";
	}


}
