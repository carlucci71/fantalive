package com.daniele.fantalive.bl;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.daniele.fantalive.util.Constant;

public class LiveGazzetta {
	public static void main(String[] args) throws Exception {
		Constant constant=null;
		Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
		Method method = cl.getDeclaredMethod("constant");
		constant = (Constant) method.invoke(constant);		
		Main.init(null,null,constant, false, "8080", null);
		Map<String, Map<String, Object>> snapPartite=new LinkedHashMap();
		Map<String, Object> jsonToMap;
		//https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=1&competitionId=21&day=23
		String callHTTP= (String) Main.callHTTP("GET", "application/json; charset=UTF-8", "https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=" + Constant.SPORT_ID_LIVE_GAZZETTA + "&competitionId=" + Constant.COMP_ID_LIVE_GAZZETTA + "&day=" + constant.GIORNATA, null).get("response");
		jsonToMap = Main.jsonToMap(callHTTP);
		List<Map> l = (List<Map>) ((Map)jsonToMap.get("data")).get("games");
		System.out.println(l);
	}

}
