package com.daniele.fantalive.bl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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
		
		List<Map<String, Object>> go = go("realfantacomix21");
		list2csv(go);
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
		Main.init(null,null,constant, false);
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
		Map<Integer, Double> getFantaValoreMercato = getFantaValoreMercato();
		for (Map<String, Object> map : list) {
			map.put("FantaValoreMercato", getFantaValoreMercato.get(map.get("id")));
		}
		return list;
	}	

	private static Map<Integer, Double> getFantaValoreMercato() throws Exception {
		Map<Integer, Double> m = new HashMap<>();
		CloseableHttpResponse  response;
		BasicCookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie;

		cookie = new BasicClientCookie("fantacalcio.it", "0xec8mnTOhuVhCb%2BeZFHYya4Co99jgjtI0NmVJLkvh2dVE%2F7%2FwAS6QzGHGR3J0bGgKTVqhnwZMECJsOS%2F6iO7cIh8hsipePZOwBDGIp2nVLt9tmRSLtKCI7VOcafKIdgi%2FWzBrPfTWomI1RrTSRGAjqMu9jNaSHg07K8StogKv8k1peTaXfY2aJQNf7bRJ9HicrGsS0q4XkidfpOhqGzmL0XeIKFiLr6QzRqLSsXZkoCVduynfnDyeP%2FNwxlZEQNB7J8qOtYRAoexWKtWOW4PnMq1zTPeWzz3MpmqCEWKqdc5hopDD%2FzWr8u25xIRqgqQqrfLhl3DnJOiZOM58Q%2BNo52CUcir0eBAEKnOhCwNs7zC61hStg5u25LXD%2FPGRonUC%2B6cWXEVbr%2FCcJDwQtc8gqYgTBWOD2zivWlKPfwBXrLlmwtZCqaKH127WntcnrKX%2B2fos0T6%2FOR5jBMEaHdVXEDggvBr6kM");
		cookie.setDomain("www.fantacalcio.it");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		try {
			String uri = "https://www.fantacalcio.it/api/v1/Excel/prices/17/1";
			HttpGet httpget = new HttpGet(uri);

			new ResponseHandler<String>() {
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
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				int riga = 0;
				try (XSSFWorkbook myWorkBook= new XSSFWorkbook (entity.getContent())) {
					XSSFSheet mySheet = myWorkBook.getSheetAt(0);
					Iterator<Row> rowIterator = mySheet.iterator(); 
					while (rowIterator.hasNext()) {
						riga++;
						Row row = rowIterator.next();
						if (riga >2) {
							Integer k =((Double)row.getCell(0).getNumericCellValue()).intValue();
							m.put(k, row.getCell(11).getNumericCellValue());
							/*
							Iterator<Cell> cellIterator = row.cellIterator();
							int colonna=0;
							while (cellIterator.hasNext()) {
								colonna++;
								Cell cell = cellIterator.next();
								if (colonna == 1 || colonna == 12) {
									if (cell.getCellType().equals(CellType.STRING)) {
//										System.out.print(cell.getStringCellValue() + "\t");
									}
									else if (cell.getCellType().equals(CellType.NUMERIC)) {
										if (k==null) {
//											System.out.print(cell.getNumericCellValue() + "\t");
										} else {
											m.put(k, cell.getNumericCellValue());
										}
//										System.out.print(cell.getNumericCellValue() + "\t");
									}
									else if (cell.getCellType().equals(CellType.BLANK)) {
//										System.out.print(" " + "\t");
									}
									else {
										throw new RuntimeException("Tipo non gestito");
									}
								}
							}
//							System.out.println(); 
							*/
						}
					}
				}
			}
//			System.out.println();
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			throw e;
		}
		finally {
			httpclient.close();
		}
//		System.out.println(response);
		//	Map<String, Object> jsonToMap = Main.jsonToMap(responseBody);


		//	h.put("fantacalcio.it", "0xec8mnTOhuVhCb%2BeZFHYya4Co99jgjtI0NmVJLkvh2dVE%2F7%2FwAS6QzGHGR3J0bGgKTVqhnwZMECJsOS%2F6iO7cIh8hsipePZOwBDGIp2nVLt9tmRSLtKCI7VOcafKIdgi%2FWzBrPfTWomI1RrTSRGAjqMu9jNaSHg07K8StogKv8k1peTaXfY2aJQNf7bRJ9HicrGsS0q4XkidfpOhqGzmL0XeIKFiLr6QzRqLSsXZkoCVduynfnDyeP%2FNwxlZEQNB7J8qOtYRAoexWKtWOW4PnMq1zTPeWzz3MpmqCEWKqdc5hopDD%2FzWr8u25xIRqgqQqrfLhl3DnJOiZOM58Q%2BNo52CUcir0eBAEKnOhCwNs7zC61hStg5u25LXD%2FPGRonUC%2B6cWXEVbr%2FCcJDwQtc8gqYgTBWOD2zivWlKPfwBXrLlmwtZCqaKH127WntcnrKX%2B2fos0T6%2FOR5jBMEaHdVXEDggvBr6kM");
		return m;

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
}
