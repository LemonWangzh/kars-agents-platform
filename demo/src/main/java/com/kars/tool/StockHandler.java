package com.kars.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Slf4j
public class StockHandler {

    @Tool
    public String handler(@P("股票名称") String stockName,
                          @P("股票编号") String stockNum,
                          @P("股票价格") String price) throws IOException {
        System.out.println("stockName=" + stockName);
        System.out.println("stockNum=" + stockNum);
        System.out.println("price=" + price);

        var baseUrl = "n278m3xy9n.re.qweatherapi.com";
        var location = "101010100";
        var msg = "fail";
        String url = String.format("https://%s/v7/weather/now?location=%s", baseUrl, location);
        String apiKey = System.getenv("WEATHER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("WEATHER_API_KEY not set, skipping test");
            return msg;
        }
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-QW-Api-Key", apiKey)
                .build();
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if (response.body() == null) {
                System.out.println("error: empty response body");
                return msg;
            }
            System.out.println(response.body().string());
        }
        return "股票分析完成009";
    }

}
