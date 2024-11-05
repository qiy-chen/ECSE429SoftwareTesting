package com.testproject;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import io.cucumber.java.lu.an;

public class TodoStepDefinitions {
    static final String baseURL = "http://localhost:4567/";
    static final String TODOS = "todos";
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();

    static final int SUCESS_CODE = 200;
    static final int CREATED_CODE = 201;
    static final int BAD_REQUEST_CODE = 400; 
    static final int NOT_FOUND_CODE = 404; 
    static final int NOT_ALLOWED_CODE = 405; 
    
    int nb_instances;
    HashMap<String, Object> new_instance;
    HashMap<String, Object> new_request_body;
    int returnCode;
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

    @Given("the affected instance is non-existent")
    public void set_request_to_inexistant_instance() throws URISyntaxException, IOException, InterruptedException {
      new_request_body.put("id", -1);
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
      returnCode = response_post.statusCode();
      }

    @When("I send a request to the server to modify the instance")
    public void send_modify_request() throws URISyntaxException, IOException, InterruptedException {
        // If id not set in request, use same as current instance
        String instance_id = (String) String.valueOf(new_request_body.get("id"));
        if (instance_id == "null") {
          instance_id = (String) String.valueOf(new_instance.get("id"));
        }
        // Remove id field from object
        new_request_body.remove("id");

        // Generate payload
        String request_body = helper.mapToJSONString(new_request_body);
        HttpRequest post_request = request.uri(new URI(baseURL + endpoint_name + "/" + instance_id))
                                        .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                        .build();
        HttpResponse<String> response_post = client.send(post_request, BodyHandlers.ofString());
        new_instance = helper.jsonStringToMap(response_post.body());
        new_request_body = new HashMap<String, Object>();
        returnCode = response_post.statusCode();
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
        // HttpRequest test_request_cleanup = request.uri(new URI(baseURL + endpoint_name + "/" + new_instance.get("id")))
        // .DELETE()
        // .build();
        // client.send(test_request_cleanup, BodyHandlers.ofString());
        assertEquals(nb_instances + 1, todos_instances.size());
      }

      @Then("I do not see any additionnal instance in my {string} list")
      public void result_no_additionnal_instance(String name) throws IOException, InterruptedException, URISyntaxException{
        
        HttpRequest send_request = request.uri(new URI(baseURL + endpoint_name))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(send_request, BodyHandlers.ofString());
        @SuppressWarnings("rawtypes")
        List todos_instances = (List) helper.jsonStringToMap(response.body()).get(endpoint_name);

        assertEquals(nb_instances, todos_instances.size());
      }

      @Then("I receive an error not found")
      public void result_post_not_found_error(){
        assertEquals(NOT_FOUND_CODE, returnCode);
      }

      
      @Then("I receive an error of invalid request")
      public void result_post_invalid_error(){
        assertEquals(BAD_REQUEST_CODE, returnCode);
      }

      @Then("the {string} is equal to {string}")
      public void result_check_field_value(String field, String string_value) throws IOException, InterruptedException, URISyntaxException{
        System.out.println(returnCode);
        System.out.println(new_instance);
        assertEquals(string_value, new_instance.get(field));
      }
}
