package com.testproject;
import org.apache.commons.collections.CollectionUtils;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


import java.util.Arrays;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

@TestMethodOrder(MethodOrderer.Random.class)
public class todoCategoriesTest {
    static final String baseURL = "http://localhost:4567/todos";
    static final String categoriesURL = "http://localhost:4567/categories";
    static HashMap<String, Object> test_instance = null;
    static HashMap<String, Object> test_category_related_instance = null;
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();

    static final int SUCESS_CODE = 200;
    static final int CREATED_CODE = 201;
    static final int BAD_REQUEST_CODE = 400; 
    static final int NOT_FOUND_CODE = 404; 
    static final int NOT_ALLOWED_CODE = 405; 

    static final String TODOS = "todos";
    static final String CATEGORIES = "categories";

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

    @BeforeEach
    public void createTestInstance() throws Throwable{
        try{
            String request_test_body = """
            {title: "test entity", doneStatus: false, description: "desc"}
            """;
            String request_category_relation_test_body = """
            {id: "1"}
            """;
            HttpRequest test_instance_request = request.uri(new URI(baseURL))
                                        .POST(HttpRequest.BodyPublishers.ofString(request_test_body))
                                        .build();
            HttpResponse<String> response_todo = client.send(test_instance_request, BodyHandlers.ofString());
            HashMap<String, Object> response_todo_test_instance = helper.jsonStringToMap(response_todo.body());

            HttpRequest test_instance_add_category_request = request.uri(new URI(baseURL + "/" + response_todo_test_instance.get("id") + "/categories") )
                                        .POST(HttpRequest.BodyPublishers.ofString(request_category_relation_test_body))
                                        .build();
            client.send(test_instance_add_category_request, BodyHandlers.ofString());
            
            // Set the test instances
            test_instance = response_todo_test_instance;
            test_category_related_instance = helper.jsonStringToMap("""
                {id: "1", title: "Office", description: ""}
            """);
        }
        catch (Throwable  e){
            System.out.println(e);
            throw e;
        }
    }

    @AfterEach
    public void deleteTestInstance() throws Throwable{
        try{
            HttpRequest test_instance_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") ))
                                        .DELETE()
                                        .build();
            client.send(test_instance_request, BodyHandlers.ofString());
            // Set the test instance
            test_instance = null;
            test_category_related_instance = null;
        }
        catch (Throwable  e){
            System.out.println(e);
            throw e;
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void getAllTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> expected_response = new HashMap<String, Object>();
        expected_response.put(CATEGORIES, Arrays.asList(test_category_related_instance));
        
        // assert the response in JSON is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertTrue(CollectionUtils.isEqualCollection((List) expected_response.get(CATEGORIES), 
        (List) helper.jsonStringToMap(response.body()).get(CATEGORIES)));

        // Check if payload in XML can be generated correclty
        HttpRequest test_request_xml = request.setHeader("Accept", "application/xml")
                                        .build();
        HttpResponse<String> response_xml = client.send(test_request_xml, BodyHandlers.ofString());

        // Go back to JSON payloads
        request.setHeader("Accept", "application/json");
        String expected_response_xml_string = """
                    <categories>
                        <category>
                            <description/>
                            <id>1</id>
                            <title>Office</title>
                        </category>
                    </categories>
                """;

        int differencesSize = helper.getDifferencesSizeXMLStrings(expected_response_xml_string, response_xml.body());

        // assert the response in XML is correct
        assertEquals(SUCESS_CODE, response_xml.statusCode());
        assertEquals(differencesSize, 0);
    }

    @Test
    public void putTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "1"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .PUT(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void postTestSuccess() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "2"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // Get added category data
        HttpRequest added_category = request.uri(new URI(categoriesURL + "/2"))
                                    .GET()
                                    .build();
        HttpResponse<String> response_added_category = client.send(added_category, BodyHandlers.ofString());

        // After the request state
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(CATEGORIES);
        expected_new_todos_list.add(helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap(response_added_category.body()), CATEGORIES));
        System.out.println(expected_new_todos_list);
        System.out.println(helper.jsonStringToMap(response_side_effect_after.body()).get(CATEGORIES));

        // Remove newly created relation
        HttpRequest test_request_cleanup = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories/2"))
                                    .DELETE()
                                    .build();
        client.send(test_request_cleanup, BodyHandlers.ofString());

        // assert the new relation is correct
        assertEquals(CREATED_CODE, response.statusCode());

        // assert the relations are correct
        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
                    (List) helper.jsonStringToMap(response_side_effect_after.body()).get(CATEGORIES)));
        
    }

    @SuppressWarnings({ "rawtypes"})
    @Test
    public void postExistingRelationTestSuccess() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "1"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // After the request state
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(CATEGORIES);

        // assert the new relation is correct
        assertEquals(CREATED_CODE, response.statusCode());

        // assert the relations are correct
        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
                    (List) helper.jsonStringToMap(response_side_effect_after.body()).get(CATEGORIES)));
        
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void postMalformedJSONTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "2",}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["com.google.gson.stream.MalformedJsonException: Expected name at line 1 column 11 path $."]}
            """
        );
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(CATEGORIES);

        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
                    (List) helper.jsonStringToMap(response_side_effect_after.body()).get(CATEGORIES)));
    }


    @SuppressWarnings({ "rawtypes" })
    @Test
    public void postmalformedXMLTestFail() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                <
                """;

        // Set content to XML
        request.setHeader("Content-Type", "application/xml")
                .setHeader("Accept", "application/xml");

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                .POST(HttpRequest.BodyPublishers.ofString(request_body))
                .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // Revert to JSON
        request.setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");

        String expected_response = """
                                    <errorMessages><errorMessage>Misshaped element at 3 [character 1 line 2]</errorMessage></errorMessages>
                                    """;
        
        int differencesSize = helper.getDifferencesSizeXMLStrings(expected_response, response.body());
        System.out.println(response.body());
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(differencesSize, 0);

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                .GET()
                .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(CATEGORIES);

        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
        (List) helper.jsonStringToMap(response_side_effect_after.body()).get(CATEGORIES)));

    }

    @Test
    public void postExtraFieldsTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: 2, title: "random entity"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["Could not find thing matching value for id"]}
            """
        );

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());

        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));

        // Check if response is correct
        assertEquals(NOT_FOUND_CODE, response.statusCode());
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));
    }

    @Test
    public void postNonExistentTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "-1"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["Could not find thing matching value for id"]}
            """
        );

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());

        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));

        // Check if response is correct
        assertEquals(NOT_FOUND_CODE, response.statusCode());
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));
    }

    @Test
    public void postMissingFieldsTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["title : field is mandatory"]}
            """
        );

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());

        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));

        // Check if response is correct
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));
    }

    @Test
    public void deleteTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .DELETE()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void optionsTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals("", response.body());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
        }

        @Test
        public void patchTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: 1, title: "random entity", doneStatus: true}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .method("PATCH", HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void headTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/categories" ))
                                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the response in JSON is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertNotEquals(null, response.headers());
        assertEquals("", response.body());
    }
}
