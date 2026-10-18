package com.daniele.fantalive.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FindUrlFS {
    public static void main(String[] args) throws Exception {
        String url = "https://www.fanta.soccer/it/seriea/111438/calciatore/XXXX/";
        Connection.Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .timeout(30000)
                .execute();
        System.out.println("Status: " + response.statusCode() + " " + response.statusMessage());
        System.out.println("URL finale: " + response.url());
        Document doc = response.parse();
        System.out.println("Titolo: " + doc.title());
        System.out.println(doc.html());
    }
}
