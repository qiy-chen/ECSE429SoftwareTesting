import org.apache.commons.collections.CollectionUtils;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



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
public class todoTest {
    static final String baseURL = "http://localhost:4567/todos";
    static HashMap<String, Object> test_instance = null;
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

    @BeforeEach
    public void createTestInstance() throws Throwable{
        try{
            String request_test_body = """
                {title: "test entity", doneStatus: false, description: "desc"}
                """;
            HttpRequest test_instance_request = request.uri(new URI(baseURL))
                                        .POST(HttpRequest.BodyPublishers.ofString(request_test_body))
                                        .build();
            HttpResponse<String> response = client.send(test_instance_request, BodyHandlers.ofString());

            // Get the newly created instance
            HashMap<String, Object> response_test_instance = helper.jsonStringToMap(response.body());
            // Set the test instance
            test_instance = response_test_instance;
        }
        catch (Throwable  e){
            System.out.println(e);
            throw e;
        }
    }

    @AfterEach
    public void deleteTestInstance() throws Throwable{
        try{
            HttpRequest test_instance_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id")))
                                        .DELETE()
                                        .build();
            client.send(test_instance_request, BodyHandlers.ofString());
            // Set the test instance
            test_instance = null;
        }
        catch (Throwable  e){
            System.out.println(e);
            throw e;
        }
    }

    @Test
    public void getTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> expected_response = test_instance;

        // assert the response in JSON is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals(expected_response, helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap(response.body()), "todos"));

        // Check if payload in XML can be generated correctly
        HttpRequest test_request_xml = request.setHeader("Accept", "application/xml")
                                        .build();
        HttpResponse<String> response_xml = client.send(test_request_xml, BodyHandlers.ofString());

        // Go back to JSON payloads
        request.setHeader("Accept", "application/json");

        String expected_response_xml_string = String.format("""
                <todos>
                    <todo>
                        <doneStatus>false</doneStatus>
                        <description>desc</description>
                        <id>%s</id>
                        <title>test entity</title>
                    </todo>
                </todos>
                """, test_instance.get("id")
        );

        int differencesSize = helper.getDifferencesSizeXMLStrings(expected_response_xml_string, response_xml.body());

        // assert the response in XML is correct
        assertEquals(SUCESS_CODE, response_xml.statusCode());
        assertEquals(differencesSize, 0);
    }

    @Test
    public void getNonExistentTestFail() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/-1" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {"errorMessages":["Could not find an instance with todos/-1"]}
            """
        );

        // assert the response in JSON is correct
        assertEquals(response.statusCode(), NOT_FOUND_CODE);
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));
    }

    @Test
    public void postTestSuccess() throws IOException, InterruptedException, URISyntaxException{
        String request_body = """
                {title: "test title"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> response_map_instance = helper.jsonStringToMap(response.body());

        // Expected response from the operation
        HashMap<String, Object> expected_response = new HashMap<String, Object>(test_instance);
        // Amend operation
        expected_response.put("title", "test title");

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the returned object is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals(expected_response, response_map_instance);

        // assert the object exists and there is no side-effect
        assertEquals(response_map_instance, 
            helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap( response_side_effect_after.body()), "todos"));
    }

    @Test
    public void postNonExistentTestFail() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        String request_body = """
                {title: "test title"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/-1" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["No such todo entity instance with GUID or ID -1 found"]}
            """
        );

        // assert the response in JSON is correct
        assertEquals(response.statusCode(), NOT_FOUND_CODE);
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));
    }

    @Test
    public void postBadFieldTypesTestFail() throws IOException, InterruptedException, URISyntaxException{
        String request_body = """
                {doneStatus: "hi"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> response_map_instance = helper.jsonStringToMap(response.body());

        // Expected response from the operation
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["Failed Validation: doneStatus should be BOOLEAN"]}
            """
        );

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the request is refused and proper message
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(expected_response, response_map_instance);

        // assert the object exists and there is no side-effect
        assertEquals(test_instance, 
            helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap( response_side_effect_after.body()), "todos"));
    }

    // Excpected to do an amend operation instead of full replacement due to documentation
    @Test
    public void putTestAsDocumented() throws IOException, InterruptedException, URISyntaxException{
        String request_body = """
                {title: "test title"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .PUT(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> response_map_instance = helper.jsonStringToMap(response.body());

        // Expected response from the operation
        HashMap<String, Object> expected_response = new HashMap<String, Object>(test_instance);
        // Amend operation
        expected_response.put("title", "test title");

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the returned object is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals(expected_response, response_map_instance);

        // assert the object exists and there is no side-effect
        assertEquals(response_map_instance, 
            helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap( response_side_effect_after.body()), "todos"));
    }

    @Test
    public void putTestActual() throws IOException, InterruptedException, URISyntaxException{
        String request_body = """
                {title: "test title"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .PUT(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        System.out.println(response.body());
        HashMap<String, Object> response_map_instance = helper.jsonStringToMap(response.body());

        // Expected response from the operation
        // Complete overwrite with default values
        HashMap<String, Object> expected_response = helper.jsonStringToMap(String.format(
            """
            {id: "%s", title: "test title",  description: "", doneStatus: "false"}
            """
        , (String) test_instance.get("id"))
        );

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the returned object is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals(expected_response, response_map_instance);

        // assert the object exists and there is no side-effect
        assertEquals(response_map_instance, 
            helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap( response_side_effect_after.body()), "todos"));
    }

    @Test
    public void putNonExistentTestFail() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        String request_body = """
                {title: "test title"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/-1" ))
                                    .PUT(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["Invalid GUID for -1 entity todo"]}
            """
        );

        // assert the response in JSON is correct
        assertEquals(response.statusCode(), NOT_FOUND_CODE);
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));
    }

    @Test
    public void putBadFieldTypesTestFail() throws IOException, InterruptedException, URISyntaxException{
        String request_body = """
                {doneStatus: "hi"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .PUT(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> response_map_instance = helper.jsonStringToMap(response.body());

        // Expected response from the operation
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["Failed Validation: doneStatus should be BOOLEAN"]}
            """
        );

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the request is refused and proper message
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(expected_response, response_map_instance);

        // assert the object exists and there is no side-effect
        assertEquals(test_instance, 
            helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap( response_side_effect_after.body()), "todos"));
    }

    @Test
    public void putMissingFieldsTestActualFail() throws IOException, InterruptedException, URISyntaxException{
        String request_body = """
                {doneStatus: false}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .PUT(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> response_map_instance = helper.jsonStringToMap(response.body());

        // Expected response from the operation
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["title : field is mandatory"]}
            """
        );

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the request is refused and proper message
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(expected_response, response_map_instance);

        // assert the object exists and there is no side-effect
        assertEquals(test_instance, 
            helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap( response_side_effect_after.body()), "todos"));
    }

    @Test
    public void postMalformedJSONTestFail() throws IOException, InterruptedException, URISyntaxException{
        String request_body = """
                {title: "new entity",}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["com.google.gson.stream.MalformedJsonException: Expected name at line 1 column 23 path $."]}
            """
        );
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the object exists and there is no side-effect
        assertEquals(test_instance, 
            helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap( response_side_effect_after.body()), "todos"));
    }

    @Test
    public void postmalformedXMLTestFail() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        String request_body = """
                <todo>
                    <title>
                        new entity
                    </title
                </todo>
                """;

        // Set content to XML
        request.setHeader("Content-Type", "application/xml")
                .setHeader("Accept", "application/xml");

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                .POST(HttpRequest.BodyPublishers.ofString(request_body))
                .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // Revert to JSON
        request.setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");

        String expected_response = """
                                    <errorMessages><errorMessage>Misplaced &apos;&lt;&apos; at 51 [character 1 line 5]</errorMessage></errorMessages>
                                    """;
        
        int differencesSize = helper.getDifferencesSizeXMLStrings(expected_response, response.body());
        System.out.println(response.body());
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(differencesSize, 0);

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                .GET()
                .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the object exists and there is no side-effect
        assertEquals(test_instance, 
            helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap( response_side_effect_after.body()), "todos"));

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void deleteTestSuccess() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_all_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_before = client.send(test_request_side_effect_all_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .DELETE()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // Check if properly deleted after the request (instance endpoint)
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the returned status is correct
        assertEquals(SUCESS_CODE, response.statusCode());

        // assert the object no longer exists and there is no side-effect
        assertEquals(NOT_FOUND_CODE, response_side_effect_after.statusCode());

        // Check if no side-effect after the request (all instances endpoint)
        HttpRequest test_request_side_effect_all_after = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_after = client.send(test_request_side_effect_all_after, BodyHandlers.ofString());
        List expected_initial_todos_list = (List) helper.jsonStringToMap(response_side_effect_all_after.body()).get(TODOS);
        // The new list plus the deleted entity should have the same elements as the list before the delete operation
        expected_initial_todos_list.add(test_instance);

        // assert the object is removed from the list and there is no side-effect
        assertTrue(CollectionUtils.isEqualCollection((List) helper.jsonStringToMap(response_side_effect_all_before.body()).get(TODOS), 
                                expected_initial_todos_list));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void deleteSecondTimeTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_all_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_before = client.send(test_request_side_effect_all_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .DELETE()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        HttpResponse<String> response_second_delete = client.send(test_request, BodyHandlers.ofString());

        // Check if properly deleted after the request (instance endpoint)
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());

        // assert the returned status is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals(NOT_FOUND_CODE, response_second_delete.statusCode());

        // assert the object no longer exists and there is no side-effect
        assertEquals(NOT_FOUND_CODE, response_side_effect_after.statusCode());

        // Check if no side-effect after the request (all instances endpoint)
        HttpRequest test_request_side_effect_all_after = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_after = client.send(test_request_side_effect_all_after, BodyHandlers.ofString());
        List expected_initial_todos_list = (List) helper.jsonStringToMap(response_side_effect_all_after.body()).get(TODOS);
        // The new list plus the deleted entity should have the same elements as the list before the delete operation
        expected_initial_todos_list.add(test_instance);

        // assert the object is removed from the list and there is no side-effect
        assertTrue(CollectionUtils.isEqualCollection((List) helper.jsonStringToMap(response_side_effect_all_before.body()).get(TODOS), 
                                expected_initial_todos_list));
        }

        @SuppressWarnings({"rawtypes" })
    @Test
    public void deleteNonExistentTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_all_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_before = client.send(test_request_side_effect_all_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/-1" ))
                                    .DELETE()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the returned status is correct
        assertEquals(NOT_FOUND_CODE, response.statusCode());

        // Check if no side-effect after the request (all instances endpoint)
        HttpRequest test_request_side_effect_all_after = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_after = client.send(test_request_side_effect_all_after, BodyHandlers.ofString());
        List expected_initial_todos_list = (List) helper.jsonStringToMap(response_side_effect_all_after.body()).get(TODOS);

        // assert there is no side-effect
        assertTrue(CollectionUtils.isEqualCollection((List) helper.jsonStringToMap(response_side_effect_all_before.body()).get(TODOS), 
                                expected_initial_todos_list));
        }

    @Test
    public void optionsTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals("", response.body());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void patchTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: 1, title: "random entity", doneStatus: true}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .method("PATCH", HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + (String) test_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void headTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the response in JSON is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertNotEquals(null, response.headers());
        assertEquals("", response.body());
    }
}
