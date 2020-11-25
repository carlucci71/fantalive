package scrap;

public class ConfigCampionato {
	String file;
	Integer numGiocatori;
	String tipo; 
	String campionato;
	public ConfigCampionato(String file, Integer numGiocatori, String tipo, String campionato) {
		super();
		this.file = file;
		this.numGiocatori = numGiocatori;
		this.tipo = tipo;
		this.campionato = campionato;
	}
	@Override
	public String toString() {
		return "ConfigCampionato [file=" + file + ", numGiocatori=" + numGiocatori + ", tipo=" + tipo + ", campionato=" + campionato + "]";
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public Integer getNumGiocatori() {
		return numGiocatori;
	}
	public void setNumGiocatori(Integer numGiocatori) {
		this.numGiocatori = numGiocatori;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getCampionato() {
		return campionato;
	}
	public void setCampionato(String campionato) {
		this.campionato = campionato;
	}	
	
}
