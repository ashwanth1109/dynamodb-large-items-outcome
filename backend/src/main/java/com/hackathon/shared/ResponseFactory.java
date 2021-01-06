package com.hackathon.shared;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to help with generating lambda responses
 */
public final class ResponseFactory {
    // force non-instantiability through the `private` constructor
    private ResponseFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    private static Gson gson;

    private static void createGsonIfNull() {
        if (gson == null) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
    }

    private static String getResponseBody(Object obj) {
        createGsonIfNull();
        return gson.toJson(obj);
    }

    public static APIGatewayProxyResponseEvent getResponse(Object body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpURLConnection.HTTP_OK)
                .withBody(getResponseBody(body))
                .withHeaders(headers);
    }
}
