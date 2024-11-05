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
import java.util.HashMap;
import java.util.List;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TodoStepDefinitions {
    static final String baseURL = "http://localhost:4567/";
    static final String TODOS = "todos";
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();

    
    int nb_instances;
    HashMap<String, Object> new_instance;
    HashMap<String, Object> new_request_body;
    String endpoint_name = "";

    @Given("There are {string} todos instances in my list")
    public void initial_nb_of_instances(String n){
      nb_instances = Integer.parseInt(n);
    }

    @Given("I want to create an instance of {string}")
    public void new_instance_body_create(String name){
      endpoint_name = name;
      new_instance = new HashMap<String, Object>();
      new_request_body = new HashMap<String, Object>();
    }

    @Given("I want to modify the instance of {string} with new values")
    public void new_instance_body_modify(String name){
      endpoint_name = name;
      new_request_body = new HashMap<String, Object>();
    }

    @Given("There is an instance of {string}")
    public void initial_instance_body(String name){
      endpoint_name = name;
      new_instance = new HashMap<String, Object>();
      new_request_body = new HashMap<String, Object>();
    }

    // @When("I send a request to the server to create a todo with the title {string}, the done status at {string} and the description {string}")
    // public void i_send_request_with_fields(String title, String doneStatus, String description) throws URISyntaxException, IOException, InterruptedException {
    //   // Generate payload
    //   HashMap<String, Object> request_map = new HashMap<>();
    //   request_map.put("title", title);
    //   request_map.put("doneStatus", Boolean.valueOf(doneStatus));
    //   request_map.put("description", description);
    //   String request_body = helper.mapToJSONString(request_map);
    //   System.out.println(request_body);
    //   HttpRequest post_request = request.uri(new URI(baseURL + endpoint_name))
    //                                   .POST(HttpRequest.BodyPublishers.ofString(request_body))
    //                                   .build();
          
    //       // System.out.println(request_body);
    //       HttpResponse<String> response_post = client.send(post_request, BodyHandlers.ofString());
    //       new_instance_id = (String) helper.jsonStringToMap(response_post.body()).get("id");
    //   }

    @Given("the {string} is {string}")
    public void set_field_value_string(String field, String string_value) throws URISyntaxException, IOException, InterruptedException {
      // Add field to instance
      new_request_body.put(field, string_value);
    }

    @Given("the binary value of {string} is {string}")
    public void set_field_value_boolean(String field, String boolean_value) throws URISyntaxException, IOException, InterruptedException {
      // Add field to instance
      new_request_body.put(field, Boolean.parseBoolean(boolean_value));
    }

    @When("I send a request to the server to create the instance")
    public void send_create_request() throws URISyntaxException, IOException, InterruptedException {
      // Generate payload
      String request_body = helper.mapToJSONString(new_request_body);
      HttpRequest post_request = request.uri(new URI(baseURL + endpoint_name))
                                      .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                      .build();
      HttpResponse<String> response_post = client.send(post_request, BodyHandlers.ofString());
      new_instance = helper.jsonStringToMap(response_post.body());
      new_request_body = new HashMap<String, Object>();
      }

    @Given("the instance is created on the server")
    public void create_request_precondition() throws URISyntaxException, IOException, InterruptedException {
      // Generate payload
      String request_body = helper.mapToJSONString(new_request_body);
      HttpRequest post_request = request.uri(new URI(baseURL + endpoint_name))
                                      .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                      .build();
      HttpResponse<String> response_post = client.send(post_request, BodyHandlers.ofString());
      new_instance = helper.jsonStringToMap(response_post.body());
      new_request_body = new HashMap<String, Object>();
      }

      @Then("I should see the additionnal instance in my {string} list successfully")
      public void result_additionnal_instance(String name) throws IOException, InterruptedException, URISyntaxException{
        
        HttpRequest send_request = request.uri(new URI(baseURL + endpoint_name))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(send_request, BodyHandlers.ofString());
        @SuppressWarnings("rawtypes")
        List todos_instances = (List) helper.jsonStringToMap(response.body()).get(endpoint_name);

        // Remove newly created object
        HttpRequest test_request_cleanup = request.uri(new URI(baseURL + endpoint_name + "/" + new_instance.get("id")))
        .DELETE()
        .build();
        client.send(test_request_cleanup, BodyHandlers.ofString());
        assertEquals(nb_instances + 1, todos_instances.size());
      }

      @Then("I should not see the additionnal instance in my {string} list")
      public void result_no_additionnal_instance(String name) throws IOException, InterruptedException, URISyntaxException{
        
        HttpRequest send_request = request.uri(new URI(baseURL + endpoint_name))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(send_request, BodyHandlers.ofString());
        @SuppressWarnings("rawtypes")
        List todos_instances = (List) helper.jsonStringToMap(response.body()).get(endpoint_name);

        assertEquals(nb_instances, todos_instances.size());
      }

      @Then("the {string} should be equal to {string}")
      public void result_check_field_value(String field, String string_value) throws IOException, InterruptedException, URISyntaxException{
        assertEquals(string_value, new_instance.get(field));
      }
}
