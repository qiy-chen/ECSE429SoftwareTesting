package com.testproject.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.testproject.helper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TodoStepDefinitions {
    static final String baseURL = "http://localhost:4567/todos";
    static final String TODOS = "todos";
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();

    int nb_todos;
    String new_todo_id;

    @Given("There are {string} todos instances in my list")
    public void initial_nb_of_instances(String n){
      nb_todos = Integer.parseInt(n);
    }

    @When("I send a request to the server with those fields {Map<String, String>}")
    public void i_send_request_with_fields(Map<String, String> fields_values) throws URISyntaxException, IOException, InterruptedException {
      String request_body = """
        {title: "new entity", doneStatus: true}
        """;
          HttpRequest post_request = request.uri(new URI(baseURL))
                                      .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                      .build();
          HttpResponse<String> response_post = client.send(post_request, BodyHandlers.ofString());
          new_todo_id = (String) helper.jsonStringToMap(response_post.body()).get("id");
      }

      @Then("I should see the additionnal todo instance in my todos list")
      public void result_additionnal_todo_instance(String n) throws IOException, InterruptedException, URISyntaxException{
        HttpRequest send_request = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(send_request, BodyHandlers.ofString());
        @SuppressWarnings("rawtypes")
        List todos_instances = (List) helper.jsonStringToMap(response.body()).get(TODOS);
        assertEquals(todos_instances.size(), nb_todos);

        // Remove newly created object
        HttpRequest test_request_cleanup = request.uri(new URI(baseURL + "/" + new_todo_id))
                                    .DELETE()
                                    .build();
        client.send(test_request_cleanup, BodyHandlers.ofString());
      }
}
