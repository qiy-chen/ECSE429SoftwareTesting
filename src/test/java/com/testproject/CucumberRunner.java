package com.testproject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import io.cucumber.junit.platform.engine.Constants;
import io.cucumber.junit.platform.engine.Cucumber;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;

@Suite
@SelectPackages("features")
@ConfigurationParameter(
   key = Constants.GLUE_PROPERTY_NAME,
   value = "com.testproject.steps"
)
public class CucumberRunner {
    static final String baseURL = "http://localhost:4567/todos";
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();

    static final int SUCESS_CODE = 200;
    static final int CREATED_CODE = 201;
    static final int BAD_REQUEST_CODE = 400; 
    static final int NOT_FOUND_CODE = 404; 
    static final int NOT_ALLOWED_CODE = 405; 

    static final String TODOS = "todos";

    @BeforeAll
    public static void startup() throws Throwable{
        try{
            HttpRequest init_ping_request = request.uri(new URI(baseURL)).build();
            client.send(init_ping_request, BodyHandlers.ofString());
        }
        catch (Throwable  e){
            System.out.println(e);
            throw e;
        }
    }


}
