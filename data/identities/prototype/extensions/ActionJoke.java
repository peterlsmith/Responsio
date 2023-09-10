//package com.paradoxwebsolutions.identity.prototype;

import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.ClientResponse;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ActionJoke implements Action {

    public void perform(Assistant assistant, ClientSession session, ClientResponse clientResponse) {

        /* Make a call to the joke API */

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://v2.jokeapi.dev/joke/Any?safe-mode&format=json"))
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());


            /* Parse out the response and get the joke */

            JsonObject joke = JsonParser.parseString(response.body()).getAsJsonObject();
            if (joke.has("joke"))
                clientResponse.utter(joke.get("joke").getAsString());
            else
                clientResponse.utter(joke.get("setup").getAsString() + "\n" + joke.get("delivery").getAsString());
        }
        catch (Exception x) {
            clientResponse.utter("I can't seem to find my joke book today");
        }

    }
}