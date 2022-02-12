package com.daniele.fantalive.util;

import java.util.List;
import java.util.Map;

import com.daniele.fantalive.bl.Main;

public class RicercaTag {
	public static void main(String[] args) throws Exception {
		Main.init(null,null,null, false);
		for (int i=1;i<2222;i++) {
			try {
				String http = (String) Main.callHTTP("GET", "application/json; charset=UTF-8",String.format(Constant.URL_API_GAZZETTA, i), null).get("response");
				Map<String, Object> jsonToMap = Main.jsonToMap(http);
				List<Map> l = (List<Map>) ((Map)jsonToMap.get("data")).get("games");
				for (Map map : l) {
					List<Map> lm = (List<Map>) map.get("matches");
					if (lm != null) {
						for (Map map2 : lm) {
							Map timing = (Map)map2.get("timing");
							if (timing == null || timing.get("tag") == null) continue;
							String tag = timing.get("tag").toString();
							if (tag.equalsIgnoreCase("FullTime") || tag.equalsIgnoreCase("Postponed") || tag.equalsIgnoreCase("PreMatch")
									|| tag.equalsIgnoreCase("Cancelled") || tag.equalsIgnoreCase("SecondHalf")) continue;
							System.out.println(i);
							System.out.println("TAG :" + tag);
							Object valTiming=timing.get("val");
							if (timing.get("val") != null) {
								System.out.println("VAL :" + valTiming.toString());

							}
						}
					}
				}
			}
			catch (Exception e) {

			}
		}
	}

}
