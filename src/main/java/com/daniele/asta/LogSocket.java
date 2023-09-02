package com.daniele.asta;

import java.util.List;

public class LogSocket {
	private String id;
//	private String jSessionId;
	private String pagina;
	private List<String> handAgent;
//	private Long handDate;
//	private Long handExpire;
//	private Long handIfModifiedSince;
//	private Long handIfUnmodifiedSince;
//	private Long handLastModify;
//	private String handOrigin;
//	private String local;
	private String remote;
	@Override
	public String toString() {
		return "LogSocket [id=" + id + ", pagina=" + pagina + ", handAgent=" + handAgent + ", remote=" + remote + "]";
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPagina() {
		return pagina;
	}
	public void setPagina(String pagina) {
		this.pagina = pagina;
	}
	public List<String> getHandAgent() {
		return handAgent;
	}
	public void setHandAgent(List<String> handAgent) {
		this.handAgent = handAgent;
	}
	public String getRemote() {
		return remote;
	}
	public void setRemote(String remote) {
		this.remote = remote;
	}
}
