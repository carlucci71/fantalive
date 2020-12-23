package fantalive.util;

import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fantalive.bl.Main;
import fantalive.model.Giocatore;
import fantalive.model.Squadra;

public class RiallineaDB {
	public static void main(String[] args) throws Exception {
		Constant c=null;
		
		Class<?> cl = Class.forName("fantalive.util.ConstantDev");
		Method method = cl.getDeclaredMethod("constant");
		c = (Constant) method.invoke(c);		
		
		Main.init(null,null,c);
		Class.forName("org.postgresql.Driver");
		Class.forName("com.mysql.jdbc.Driver");
		String getenv = System.getenv("PG_DB_URL");
		Connection conPG = DriverManager.getConnection(getenv);
		PreparedStatement prepareStatement = conPG.prepareStatement("select * from salva");
		conPG.setAutoCommit(false);
		ResultSet executeQuery = prepareStatement.executeQuery();
		Connection conMysql = DriverManager.getConnection("jdbc:mysql://localhost:3306/asta?user=asta&password=asta");
		PreparedStatement prepareStatementDEL = conMysql.prepareStatement("delete from salva");
		prepareStatementDEL.execute();
		PreparedStatement prepareStatementINS = conMysql.prepareStatement("insert into salva (nome, testo) values (?,?)");
		while(executeQuery.next()) {
			String nome = executeQuery.getString("nome");
			Clob testo = executeQuery.getClob("testo");
			System.out.println(nome);
			if (nome.startsWith(Constant.FORMAZIONE)) {
				String contenuto = testo.getSubString(1, (int) testo.length());
				List<Squadra> squadre = Main.jsonToSquadre(contenuto);
				for (Squadra squadra : squadre) {
					for (Giocatore giocatore : squadra.getTitolari()) {
						giocatore.setCodEventi(new ArrayList<>());
						giocatore.setOrario(null);
						giocatore.setId(null);
						giocatore.setNomeLive(null);
						giocatore.setRuoloLive(null);
						giocatore.setVoto(0);
						giocatore.setSquadraGioca(false);
						giocatore.setEvento("");
						giocatore.setModificatore(0);
						giocatore.setCambio(false);
						giocatore.setCambiato(false);
						giocatore.setIdGioc(null);
						giocatore.setNotificaLive(false);
					}
					for (Giocatore giocatore : squadra.getRiserve()) {
						giocatore.setCodEventi(new ArrayList<>());
						giocatore.setOrario(null);
						giocatore.setId(null);
						giocatore.setNomeLive(null);
						giocatore.setRuoloLive(null);
						giocatore.setVoto(0);
						giocatore.setSquadraGioca(false);
						giocatore.setEvento("");
						giocatore.setModificatore(0);
						giocatore.setCambio(false);
						giocatore.setCambiato(false);
						giocatore.setIdGioc(null);
						giocatore.setNotificaLive(false);
					}
				}
				contenuto=Main.toJson(squadre);
			}
			prepareStatementINS.setString(1, nome);
			prepareStatementINS.setClob(2, testo);
			prepareStatementINS.execute();
		}
	
		
		
	}
	
	

}
