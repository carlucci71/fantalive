package com.daniele.fantalive.bl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.daniele.fantalive.util.Constant;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LiveGazzetta {
	public static void main(String[] args) throws Exception {
		Map<String, Map<String, Object>> snapPartite=new LinkedHashMap();
		Map<String, Object> jsonToMap;
		Main.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		Main.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		int i=0;
//		System.out.println("[");
		while (i<1) {
			String callHTTP= (String) Main.callHTTP("GET", "application/json; charset=UTF-8", 
					"https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=" + Constant.SPORT_ID_LIVE_GAZZETTA + "&competitionId=" + Constant.COMP_ID_LIVE_GAZZETTA + "&day=" + 5
					, null).get("response");
			jsonToMap = Main.jsonToMap(callHTTP);
			List<Map> l = (List<Map>) ((Map)jsonToMap.get("data")).get("games");
	        ZoneId zoneId = ZoneId.of("Europe/Rome");
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId);
	        for (Map map : l) {
				List<Map> lm = (List<Map>) map.get("matches");
				for (Map map2 : lm) {
					Map awayTeam = (Map)map2.get("awayTeam");
					Map homeTeam = (Map)map2.get("homeTeam");
					String sqFuori = ((String)awayTeam.get("teamCode")).toUpperCase();
					String sqCasa = ((String)homeTeam.get("teamCode")).toUpperCase();
					if (sqFuori.equals("MONZ")) sqFuori="MON";
					if (sqCasa.equals("MONZ")) sqCasa="MON";
//					if (!sqCasa.equals("UDI")) continue;
					if (!sqCasa.equals("TOR")) continue;

					String status=(String) map2.get("status");
					Map timing=(Map) map2.get("timing");
					String tag=timing.get("tag").toString();
					String val=timing.get("val").toString();
					List<Map> stats = (List) timing.get("stat");
					System.out.println("***************************************");
					System.out.println("timeStampOpta" + " -> " + Instant.parse(map2.get("timeStampOpta").toString()).atZone(zoneId).format(formatter));
					if (map2.get("finalMatchDateTime") != null) {
						System.out.println("finalMatchDateTime" + " -> " + Instant.parse(map2.get("finalMatchDateTime").toString()).atZone(zoneId).format(formatter));
					}
					
			        System.out.println(formatter.format(Instant.now()));
					System.out.println("status" + " -> " + status);
					System.out.println("tag" + " -> " + tag);
					System.out.println("val" + " -> " + val);
					for (Map stat : stats) {
						String type=stat.get("Type").toString();
						if (type.equals("first_half_start_utc") || type.equals("first_half_stop_utc") || type.equals("second_half_start_utc") || type.equals("second_half_stop_utc")) {
							continue;
						}
						if (type.equals("match_time") || type.equals("first_half_time") || type.equals("second_half_time")) {
							String value = stat.get("value").toString();
							System.out.println(type + " -> " + value);
						} else {
							String value = stat.get("value").toString();
							System.out.println(type + " -> " + Instant.parse(value).atZone(zoneId).format(formatter));
						}
					}
					System.out.println();
					/*
					Map<String, Object> partite = new LinkedHashMap();
					Map timing = (Map)map2.get("timing");
					String tag = (String)timing.get("tag");
					Object valTiming=timing.get("val");
					if (timing.get("val") != null) {
						valTiming= valTiming.toString();

					}
					else {
						valTiming="N/A";
					}
					String first_half_stop=null;
					String second_half_start=null;
					List<Map> stats = (List<Map>) timing.get("stat");
					if (stats != null) {
						for (Map stat : stats) {
							if ("first_half_stop".equalsIgnoreCase(stat.get("Type").toString())) {
								first_half_stop = (String) stat.get("value");
							}
							if ("second_half_start".equalsIgnoreCase(stat.get("Type").toString())) {
								second_half_start = (String) stat.get("value");
							}
						}
					}
					partite.put("first_half_stop",first_half_stop);
					partite.put("second_half_start",second_half_start);
					partite.put("tag",tag);
					partite.put("val", valTiming.toString());
					Map<String, Object> sq = new HashMap<>();
					sq.put("gol", homeTeam.get("score"));
					List<Map> goals = (List<Map>) ((Map)homeTeam.get("starData")).get("goals");
					List<Map> reti = new ArrayList<>();
					for (Map goal : goals) {
						Map rete = new LinkedHashMap<>();
						rete.put("tipo", goal.get("goalType"));
						rete.put("goalTimestamp", goal.get("goalTimestamp"));
						rete.put("minuto", goal.get("goalAbsoluteTime"));
						rete.put("giocatore", ((Map)goal.get("goalPlayer")).get("playerName"));
						reti.add(rete);
					}
					sq.put("RETI", reti);
					partite.put(sqCasa, sq);
					sq = new HashMap<>();
					sq.put("gol", awayTeam.get("score"));
					goals = (List<Map>) ((Map)awayTeam.get("starData")).get("goals");
					reti = new ArrayList<>();
					for (Map goal : goals) {
						Map rete = new LinkedHashMap<>();
						rete.put("tipo", goal.get("goalType"));
						rete.put("goalTimestamp", goal.get("goalTimestamp"));
						rete.put("minuto", goal.get("goalAbsoluteTime"));
						rete.put("giocatore", ((Map)goal.get("goalPlayer")).get("playerName"));
						reti.add(rete);
					}
					sq.put("RETI", reti);
					partite.put(sqFuori, sq);
					String key = sqCasa + " vs " + sqFuori;
					snapPartite.put(key, partite);
					*/
				}
			}
			/*
			 * System.out.println(snapPartite);
			 * System.out.println(",");
			 */
			
			Thread.currentThread().sleep(5000);
		}
	}

}
