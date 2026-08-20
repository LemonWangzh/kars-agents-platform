package com.kars.service.third;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;


class WeatherServiceTest {

    @Test
    void test1() throws IOException {
        var baseUrl = "n278m3xy9n.re.qweatherapi.com";
        var location = "101010100";
        String url = String.format("https://%s/v7/weather/now?location=%s", baseUrl, location);
        String apiKey = System.getenv("WEATHER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("WEATHER_API_KEY not set, skipping test");
            return;
        }
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-QW-Api-Key", apiKey)
                .build();
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if (response.body() == null) {
                System.out.println("error: empty response body");
                return;
            }
            System.out.println(response.body().string());
        }
    }

}
