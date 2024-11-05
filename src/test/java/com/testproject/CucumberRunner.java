package com.testproject;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.junit.platform.engine.Constants;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.List;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/testproject")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "html:target/cucumber.html")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.testproject")
public class CucumberRunner {
    static final String baseURL = "http://localhost:4567/todos";
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();

    static final String TODOS = "todos";

    @Before
    public static void startup() throws Throwable{
        try{
            HttpRequest init_ping_request = request.uri(new URI(baseURL)).build();
            client.send(init_ping_request, BodyHandlers.ofString());
            // Clear default todo instances
            cleanup();
        }
        catch (Throwable  e){
            System.out.println(e);
            throw e;
        }
    }

    @After
    public static void cleanup() throws Throwable{
        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        @SuppressWarnings({ "unchecked" })
        List<LinkedTreeMap<String, Object>> todos_list= (List<LinkedTreeMap<String, Object>>) helper.jsonStringToMap(response.body()).get(TODOS);
        // Cleanup all instances of todos
        for (int i = 0; i < todos_list.size(); i++){
            // Convert to hashmap
            @SuppressWarnings("unchecked")
            HashMap<String, Object> instance = new Gson().fromJson(new Gson().toJson(((LinkedTreeMap<String, Object>) todos_list.get(i))), HashMap.class);
            HttpRequest delete_request = request.uri(new URI(baseURL + "/" + (String) instance.get("id") ))
                                .DELETE()
                                .build();
            client.send(delete_request, BodyHandlers.ofString());
            
        }
    }

}
