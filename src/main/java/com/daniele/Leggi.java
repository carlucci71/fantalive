package com.daniele;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Leggi {
	public static void main(String[] args) throws Exception {
		List<String> readAllLines = Files.readAllLines(Paths.get(ClassLoader.getSystemResource("./datiFCJ.txt").toURI()));
		
		StringBuilder sb = new StringBuilder();
		for (String linea : readAllLines) {
			sb.append(linea);
		}
		Map<String, Object> jsonToMap = jsonToMap(sb.toString());
		List<Map<String, Object>> l = (List<Map<String, Object>>) jsonToMap.get("data");
		StringBuilder sb2 = new StringBuilder();
		for (Map<String, Object> object : l) {
			sb2.append(object.get("n") + ";");
			sb2.append(object.get("si") + ";");
			sb2.append(object.get("r") + ";");
			sb2.append(object.get("ci") + ";");
			sb2.append(object.get("ca") + ";");
			sb2.append(object.get("mv") + ";");
			sb2.append(object.get("mfv") + "\n");
		}
		Files.write(Paths.get("/1/d.csv"), sb2.toString().getBytes());
	}
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	private static Map<String, Object> jsonToMap(String json) {
		try {
			return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
