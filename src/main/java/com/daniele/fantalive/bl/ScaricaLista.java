package com.daniele.fantalive.bl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;

import com.daniele.fantalive.util.Constant;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ScaricaLista {

	public static void main(String[] args) throws Exception {
		Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.INFO);
		/*
		List<Map<String, Object>> go = go("realfantacomix21");
		list2csv(go);
		*/
		Map<Integer, Object> getFantaValoreMercato = Main.getFantaValoreMercato(true);
		
		Set<String> nomiFg=new HashSet<>();
		
		Set<Integer> keySet = getFantaValoreMercato.keySet();

		Map<String, Map<String, Map<String, Object>>> attributiFGByNome=new HashMap<>();
		
		
		for (Integer key : keySet) {
			nomiFg.add(((Map)getFantaValoreMercato.get(key)).get("nome") + "@" + ((Map)getFantaValoreMercato.get(key)).get("squadra"));
//			"ruolo", "value", "nome", "squadra"
			Map value = (Map)getFantaValoreMercato.get(key);
			value.put("codiceFG", key);
			attributiFGByNome.put(((Map)getFantaValoreMercato.get(key)).get("nome").toString(), value);
			
		}
		System.out.println("codiceFG@nomeFS_ORIGINALE@nomeFS@nomeFG@squadrafg@squadraFS@codiceFS@ruoloFS@quotazioneFS@ruoloFG@quotazioneFG@FVM");
		
		List<Map<String, Object>> leggiQuotazioniFS = leggiQuotazioniFS();
		Map<Integer, Double> aggFVM=new HashMap<>();
		for (Map<String,Object> quotazioniFS : leggiQuotazioniFS) {
			String nOrig=quotazioniFS.get("cognome") + " " + quotazioniFS.get("nome");
//			if (nOrig.equals("Mbala ")) 
			{
				String n=Main.cambiaNomi(nOrig, quotazioniFS.get("squadra").toString().substring(0,3));
				String nomeFromFG = Main.getNomeFromFG(n, nomiFg);
				Map<String, Object> map2 = new HashMap();
				if (nomeFromFG!=null) {
					Map<String, Map<String, Object>> map = attributiFGByNome.get(nomeFromFG.split("@")[0]);
					Set<String> keySet2 = map.keySet();
					for (String string : keySet2) {
						map2.put(string, map.get(string));
					}
//					map2 = attributiFGByNome.get(nomeFromFG.split("@")[0]);
				}
				System.out.println(map2.get("codiceFG") + "@" + nOrig + "@" + n + "@" + (nomeFromFG==null?"@":nomeFromFG) + "@" + quotazioniFS.get("squadra")  + "@" + quotazioniFS.get("codice")  + "@" + quotazioniFS.get("ruolo") + "@" + quotazioniFS.get("quotazione")
				+ "@" + map2.get("ruolo") + "@" + map2.get("qa")+ "@" + map2.get("value"));
				aggFVM.put((Integer) quotazioniFS.get("codice"), (Double) map2.get("value"));
			}
		}
		aggiornaQuotazioniFS(aggFVM);
	}

	private static void list2csv(List<Map<String, Object>> jsonToList) throws Exception {
		String ROOT_FILE = "/1";
		StringBuilder sb = new StringBuilder();
		if (jsonToList.size()==0) {
			throw new RuntimeException("Nessuna riga estratta");
		} else {
			Map<String, Object> m = jsonToList.get(0);
			Set<String> keySet = m.keySet();
			String doppiApici = "\"";
			for (String key : keySet) {
				sb.append(doppiApici + key + doppiApici + ";");
			}
			sb.append("\n");

			for (Map<String, Object> map : jsonToList) {
				for (String key : keySet) {
					Object replaceAll;
					Object object = map.get(key);
					if (object == null) {
						replaceAll=null;
					}
					else if (object instanceof String) {
						//					replaceAll = object.toString().replaceAll(doppiApici, "@");
						//					replaceAll = object.toString().replaceAll(";", "ç");
						//					replaceAll = object.toString();
						replaceAll = object.toString().replaceAll(";", "ç").replaceAll(",", " ").replaceAll("\"", "");
					} else if (object instanceof Integer){
						replaceAll = object;
					} else if (object instanceof Double){
						replaceAll = String.valueOf(object).replace(".", ",");
					}
					else if (object instanceof Map){
						replaceAll = object.toString();
					}
					else if (object instanceof List){
						replaceAll = "\"" + object.toString().replaceAll(";", "ç").replaceAll("\"", "'") +  "\"" ;
						replaceAll="TBD";
					}
					else if (object instanceof Boolean){
						replaceAll = "\"" + object.toString().replaceAll(";", "ç").replaceAll("\"", "'") +  "\"" ;
						replaceAll="TBD";
					}
					else {
						throw new RuntimeException("gestire questo tipo");
						//					replaceAll = object;
					}
					if (object instanceof String) {
						sb.append(doppiApici);	
					}
					sb.append(replaceAll);
					if (object instanceof String) {
						sb.append(doppiApici);	
					}
					sb.append(";");
					//				sb.append("" + replaceAll.replace(".", ",") + "" + ";");
				}
				sb.append("\n");
			}
			Path pathFileDaGenerare = Paths.get(ROOT_FILE + "/fg.csv");
			try {
				Files.deleteIfExists(pathFileDaGenerare);
				Files.createFile(pathFileDaGenerare);
				Files.write(pathFileDaGenerare, sb.toString().getBytes());
			}catch (Exception e) {
				throw e;
			}
		}
	}


	private static List<Map<String, Object>> go(String lega) throws Exception {
		Constant constant=null;
		Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
		Method method = cl.getDeclaredMethod("constant");
		constant = (Constant) method.invoke(constant);		
		Main.init(null,null,constant, false, "8080", null);
		Main.aggKeyFG();
		Map bodyMap = new HashMap<>();
		bodyMap.put("username", constant.UTENTE_FG);
		bodyMap.put("password", constant.PWD_FG);
		Map<String, String> headers=new HashMap<>();
		headers.put("app_key", constant.APPKEY_FG_MOBILE);
		Map<String, Object> mapPutHTTP = Main.callHTTP("PUT","application/json", String.format(Constant.URL_LOGIN_FG), Main.toJson(bodyMap), headers);
		List<String> listCookie = ((List<String>)((Map)mapPutHTTP.get("headerFields")).get("Set-Cookie"));
		String cookieName = "LegheFG2_Leghe2022";
		String cookieValue=null;
		for (String cookie : listCookie) {
			int indexOf = cookie.indexOf(cookieName);
			if (indexOf>-1) {
				cookieValue=cookie.substring(cookieName.length()+1);
				cookieValue=cookieValue.substring(0,cookieValue.indexOf("; expires="));
			}
		}
		String responseBody;
		BasicCookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie;
		cookie = new BasicClientCookie(cookieName, cookieValue);
		cookie.setDomain("leghe.fantacalcio.it");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		try {
			String uri = "https://leghe.fantacalcio.it/servizi/V1_LegheCalciatori/listaSvincolatiNoMercato?alias_lega=" + lega
					+ "&t=" + "2912" + "&_=" + "1661967530283";
			HttpGet httpget = new HttpGet(uri);
			httpget.setHeader("app_key", constant.APPKEY_FG);
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};
			responseBody = httpclient.execute(httpget, responseHandler);
		} finally {
			httpclient.close();
		}
		Map<String, Object> jsonToMap = Main.jsonToMap(responseBody);
		List<Map<String, Object>> list = (List<Map<String, Object>>) jsonToMap.get("data");
		Map<Integer, Object> getFantaValoreMercato = Main.getFantaValoreMercato(false);
		for (Map<String, Object> map : list) {
			if (((Map)getFantaValoreMercato.get(map.get("id"))) != null) {
				Double xx = (Double) ((Map)getFantaValoreMercato.get(map.get("id"))).get("value");
				if (xx != null)
					map.put("FantaValoreMercato", xx);
			}
		}
		return list;
	}	

	public static Map<String,Object> callHTTP(String verbo, String contentType, String url, String body,  Map<String, String>... headers) throws Exception {
		//		System.out.println(verbo + " " + url + " " + printMap(headers));
		Map <String, Object> ret = new HashMap<>();
		URL obj = new URL(url);
		HttpURLConnection connectionHTTP = (HttpURLConnection) obj.openConnection();
		connectionHTTP.setRequestMethod(verbo);
		if (headers!=null && headers.length>0) {
			Iterator<String> iterator = headers[0].keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				connectionHTTP.setRequestProperty(key, headers[0].get(key));
			}
		}
		if (!verbo.equals("GET")) {
			connectionHTTP.setRequestProperty("content-type", contentType);
			connectionHTTP.setDoOutput(true);
			OutputStream os = connectionHTTP.getOutputStream();
			os.write(body.getBytes());
			os.flush();
			os.close();
		}
		else {
			if (body != null) {
				throw new RuntimeException("Per le chiamate con verbo GET non valorizzare il body");
			}
		}
		int responseCode=0;
		try
		{
			responseCode = connectionHTTP.getResponseCode();
		}
		catch (SSLHandshakeException e)
		{
			throw new RuntimeException("Aggiornare i certificati per: " + url);
		}
		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			Map<String, List<String>> headerFields = connectionHTTP.getHeaderFields();
			ret.put("headerFields", headerFields);
			BufferedReader in = new BufferedReader(new InputStreamReader(connectionHTTP.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} else {
			BufferedReader bfOutputResponse = new BufferedReader(
					new InputStreamReader(connectionHTTP.getErrorStream()));
			String outputLine;
			StringBuffer sfResponse = new StringBuffer();
			while ((outputLine = bfOutputResponse.readLine()) != null) {
				sfResponse.append(outputLine);
			}
			bfOutputResponse.close();
			String stringResponse = sfResponse.toString();
			throw new RuntimeException(verbo + " NOT WORKED ".concat(url).concat(" -> ").concat((body==null?"":body)).concat("STACK:")
					.concat(stringResponse));
		}
		ret.put("response", response.toString());
		return ret; 
	}
	
	private static List<Map<String, Object>> leggiQuotazioniFS() throws IOException {
		int riga=0;
		List<Map<String, Object>> lista=new ArrayList<>();
		try (HSSFWorkbook myWorkBook= new HSSFWorkbook (new FileInputStream("/1/fs.xls"))) {
			HSSFSheet mySheet = myWorkBook.getSheetAt(0);
			Iterator<Row> rowIterator = mySheet.iterator(); 
			while (rowIterator.hasNext()) {
				riga++;
				Row row = rowIterator.next();
				if (riga >1) {
					Map<String, Object> map = new HashMap<>();
					map.put("codice", ((Double)row.getCell(0).getNumericCellValue()).intValue());
					map.put("cognome", row.getCell(1).getStringCellValue());
					map.put("nome", row.getCell(2).getStringCellValue());
					map.put("squadra", row.getCell(3).getStringCellValue());
					map.put("ruolo", row.getCell(4).getStringCellValue());
					map.put("quotazione", row.getCell(6).getNumericCellValue());
					lista.add(map);
				}
			}
		}
		return lista;
	}

	private static void aggiornaQuotazioniFS(Map<Integer, Double> aggFVM) throws IOException {
		int riga=0;
		List<Map<String, Object>> lista=new ArrayList<>();
		try (HSSFWorkbook myWorkBook= new HSSFWorkbook (new FileInputStream("/1/fs.xls"))) {
			HSSFSheet mySheet = myWorkBook.getSheetAt(0);
			Iterator<Row> rowIterator = mySheet.iterator(); 
			while (rowIterator.hasNext()) {
				riga++;
				Row row = rowIterator.next();
				if (riga >1) {
					Map<String, Object> map = new HashMap<>();
					map.put("codice", ((Double)row.getCell(0).getNumericCellValue()).intValue());
					map.put("cognome", row.getCell(1).getStringCellValue());
					map.put("nome", row.getCell(2).getStringCellValue());
					map.put("squadra", row.getCell(3).getStringCellValue());
					map.put("ruolo", row.getCell(4).getStringCellValue());
					map.put("quotazione", row.getCell(6).getNumericCellValue());
					Double double1 = aggFVM.get(((Double)row.getCell(0).getNumericCellValue()).intValue());
					if (double1==null) {
						double1=new Double(0);
					} else
					{
						double1=round(double1/2,0);
					}
					row.createCell(7).setCellValue(double1);
					lista.add(map);
				} else {
					row.createCell(7).setCellValue("FVM");
				}
			}
			 FileOutputStream fileOut = new FileOutputStream("/1/fs2.xls");
			 myWorkBook.write(fileOut);

		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}	
	
}
