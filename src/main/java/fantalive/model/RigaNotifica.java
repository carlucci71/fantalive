package fantalive.model;

public class RigaNotifica {
	public RigaNotifica(int conta, String testo, String icona) {
		super();
		this.conta = conta;
		this.testo = testo;
		this.icona = icona;
	}
	private int conta;
	private String testo;
	private String icona;
	public int getConta() {
		return conta;
	}
	public void setConta(int conta) {
		this.conta = conta;
	}
	public String getTesto() {
		return testo;
	}
	public void setTesto(String testo) {
		this.testo = testo;
	}
	public String getIcona() {
		return icona;
	}
	public void setIcona(String icona) {
		this.icona = icona;
	}
	@Override
	public String toString() {
		return "RigaNotifica [conta=" + conta + ", testo=" + testo + ", icona=" + icona + "]";
	}
	
}
