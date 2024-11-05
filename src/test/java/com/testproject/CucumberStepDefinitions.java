package com.testproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CucumberStepDefinitions {
    static final String baseURL = "http://localhost:4567/";
    static final String TODOS = "todos";
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();

    static final int SUCESS_CODE = 200;
    static final int CREATED_CODE = 201;
    static final int BAD_REQUEST_CODE = 400; 
    static final int NOT_FOUND_CODE = 404; 
    static final int NOT_ALLOWED_CODE = 405; 
    
    int nb_initial_instances;
    HashMap<String, Object> new_instance;
    HashMap<String, Object> new_request_body;
    int returnCode;
    String endpoint_name = "";
    String related_endpoint_name;
    String related_instance_id;

    @Given("There are {string} todos instances in my list")
    public void initial_nb_of_instances(String n){
      nb_initial_instances = Integer.parseInt(n);
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

    @Given("I want to delete the instance of {string}")
    public void new_instance_body_delete(String name){
      endpoint_name = name;
      new_request_body = new HashMap<String, Object>();
    }

    @Given("I want to modify the instance of {string} with a relation with {string}")
    public void new_instance_relation_body(String name, String related_name){
      endpoint_name = name;
      new_request_body = new HashMap<String, Object>();
      related_endpoint_name = related_name;
    }

    @Given("There is an instance of {string}")
    public void initial_instance_body(String name){
      endpoint_name = name;
      new_instance = new HashMap<String, Object>();
      new_request_body = new HashMap<String, Object>();
    }

    @Given("the instance is related to the {string} instance {string}")
    public void initial_instance_relation(String related_name, String related_id) throws URISyntaxException, IOException, InterruptedException{
      // If id not set in request, use same as current instance
      String instance_id = (String) String.valueOf(new_request_body.get("id"));
      if (instance_id == "null") {
        instance_id = (String) String.valueOf(new_instance.get("id"));
      }

      HashMap<String, Object> relation_request_body = new HashMap<>();

      // Add the related instance id to the request
      relation_request_body.put("id", related_id);

      String request_body = helper.mapToJSONString(relation_request_body);

      HttpRequest post_request = request.uri(new URI(baseURL + endpoint_name + "/" + instance_id + "/" + related_name))
                                      .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                      .build();
      client.send(post_request, BodyHandlers.ofString());
    }

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

    @Given("the related instance is non-existent")
    public void set_request_to_inexistant_related_instance() throws URISyntaxException, IOException, InterruptedException {
      related_instance_id = "-1";
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

    @Given("the related instance is {string}")
    public void set_related_id(String related_id) {
      related_instance_id = related_id;
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

    @When("I send a request to the server to delete the instance")
    public void send_delete_request() throws URISyntaxException, IOException, InterruptedException {
        // If id not set in request, use same as current instance
        String instance_id = (String) String.valueOf(new_request_body.get("id"));
        if (instance_id == "null") {
          instance_id = (String) String.valueOf(new_instance.get("id"));
        }

        HttpRequest post_request = request.uri(new URI(baseURL + endpoint_name + "/" + instance_id))
                                        .DELETE()
                                        .build();
        HttpResponse<String> response_post = client.send(post_request, BodyHandlers.ofString());
        new_instance = helper.jsonStringToMap(response_post.body());
        new_request_body = new HashMap<String, Object>();
        returnCode = response_post.statusCode();
    }

    @When("I send a request to the server to add the relation")
    public void send_create_relation_request() throws URISyntaxException, IOException, InterruptedException {
        // If id not set in request, use same as current instance
        String instance_id = (String) String.valueOf(new_request_body.get("id"));
        if (instance_id == "null") {
          instance_id = (String) String.valueOf(new_instance.get("id"));
        }

        // Add the related instance id to the request
        new_request_body.put("id", related_instance_id);

        String request_body = helper.mapToJSONString(new_request_body);

        HttpRequest post_request = request.uri(new URI(baseURL + endpoint_name + "/" + instance_id + "/" + related_endpoint_name))
                                        .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                        .build();
        HttpResponse<String> response_post = client.send(post_request, BodyHandlers.ofString());
        new_request_body = new HashMap<String, Object>();
        returnCode = response_post.statusCode();
    }

    @When("I send a request to the server to delete the relation")
    public void send_delete_relation_request() throws URISyntaxException, IOException, InterruptedException {
        // If id not set in request, use same as current instance
        String instance_id = (String) String.valueOf(new_request_body.get("id"));
        if (instance_id == "null") {
          instance_id = (String) String.valueOf(new_instance.get("id"));
        }

        HttpRequest post_request = request.uri(new URI(baseURL + endpoint_name + "/" + instance_id + "/" + related_endpoint_name + "/" + related_instance_id))
                                        .DELETE()
                                        .build();
        HttpResponse<String> response_post = client.send(post_request, BodyHandlers.ofString());
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
        assertEquals(nb_initial_instances + 1, todos_instances.size());
      }

      @Then("I should see the instance removed from my {string} list successfully")
      public void result_removed_instance(String name) throws IOException, InterruptedException, URISyntaxException{
        
        HttpRequest send_request = request.uri(new URI(baseURL + endpoint_name))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(send_request, BodyHandlers.ofString());
        @SuppressWarnings("rawtypes")
        List todos_instances = (List) helper.jsonStringToMap(response.body()).get(endpoint_name);

        assertEquals(nb_initial_instances - 1, todos_instances.size());
      }

      @Then("I do not see any change in my {string} list")
      public void result_no_additionnal_instance(String name) throws IOException, InterruptedException, URISyntaxException{
        
        HttpRequest send_request = request.uri(new URI(baseURL + endpoint_name))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(send_request, BodyHandlers.ofString());
        @SuppressWarnings("rawtypes")
        List todos_instances = (List) helper.jsonStringToMap(response.body()).get(endpoint_name);

        assertEquals(nb_initial_instances, todos_instances.size());
      }

      @Then("the {string} is equal to {string}")
      public void result_check_field_value(String field, String string_value) throws IOException, InterruptedException, URISyntaxException{
        assertEquals(string_value, new_instance.get(field));
      }

      @Then("I see the relation between the two")
      public void result_new_relation() throws IOException, InterruptedException, URISyntaxException{
        HttpRequest send_request = request.uri(new URI(baseURL + endpoint_name + "/" + new_instance.get("id") + "/" + related_endpoint_name))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(send_request, BodyHandlers.ofString());
        @SuppressWarnings("rawtypes")
        List categories_instances = (List) helper.jsonStringToMap(response.body()).get(related_endpoint_name);
        boolean hasRelatedId = false;
        // Check if there is the related instance in the list of related instances
        for (int i = 0; i < categories_instances.size(); i++){
          @SuppressWarnings("unchecked")
          // Convert linkedtreemap to hashmap
          HashMap<String, Object> instance = new Gson().fromJson(new Gson().toJson(((LinkedTreeMap<String, Object>) categories_instances.get(i))), HashMap.class);
          if (String.valueOf(instance.get("id")).equals(String.valueOf(related_instance_id))) {
            hasRelatedId = true;
            break;
          }
        }
        assertTrue(hasRelatedId);
      }

      @Then("I do not see the relation between the two")
      public void result_deleted_relation() throws IOException, InterruptedException, URISyntaxException{
        HttpRequest send_request = request.uri(new URI(baseURL + endpoint_name + "/" + new_instance.get("id") + "/" + related_endpoint_name))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(send_request, BodyHandlers.ofString());
        @SuppressWarnings("rawtypes")
        List categories_instances = (List) helper.jsonStringToMap(response.body()).get(related_endpoint_name);
        boolean hasRelatedId = false;
        // Check if there is the related instance in the list of related instances
        for (int i = 0; i < categories_instances.size(); i++){
          @SuppressWarnings("unchecked")
          // Convert linkedtreemap to hashmap
          HashMap<String, Object> instance = new Gson().fromJson(new Gson().toJson(((LinkedTreeMap<String, Object>) categories_instances.get(i))), HashMap.class);
          if (String.valueOf(instance.get("id")).equals(String.valueOf(related_instance_id))) {
            hasRelatedId = true;
            break;
          }
        }
        assertFalse(hasRelatedId);
      }

      @Then("I receive an error not found")
      public void result_post_not_found_error(){
        assertEquals(NOT_FOUND_CODE, returnCode);
      }

      
      @Then("I receive an error of invalid request")
      public void result_post_invalid_error(){
        assertEquals(BAD_REQUEST_CODE, returnCode);
      }

      
}
