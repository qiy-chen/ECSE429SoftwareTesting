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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.xml.sax.SAXException;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import javax.xml.parsers.ParserConfigurationException;

@TestMethodOrder(MethodOrderer.Random.class)
public class todosTest {
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

    @SuppressWarnings("rawtypes")
    @Test
    public void getAllTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        HashMap<String, Object> expected_response = helper.jsonStringToMap("""
                {
                    todos: [
                        {id: "2", title: "file paperwork", doneStatus: "false", description: "", tasksof: [{id:"1"}]}, 
                        {id: "1", title: "scan paperwork", doneStatus: "false", description: "", categories: [{id:"1"}], tasksof:[{id:"1"}]}
                    ]
                }
                """);

        // assert the response in JSON is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertTrue(CollectionUtils.isEqualCollection((List) expected_response.get(TODOS), 
        (List) helper.jsonStringToMap(response.body()).get(TODOS)));

        // Check if payload in XML can be generated correclty
        HttpRequest test_request_xml = request.setHeader("Accept", "application/xml")
                                        .build();
        HttpResponse<String> response_xml = client.send(test_request_xml, BodyHandlers.ofString());

        // Go back to JSON payloads
        request.setHeader("Accept", "application/json");

        String expected_response_xml_string = """
                <todos>
                    <todo>
                        <doneStatus>false</doneStatus>
                        <description/>
                        <tasksof>
                            <id>1</id>
                        </tasksof>
                        <id>1</id>
                        <categories>
                            <id>1</id>
                        </categories>
                        <title>scan paperwork</title>
                    </todo>
                    <todo>
                        <doneStatus>false</doneStatus>
                        <description/>
                        <tasksof>
                            <id>1</id>
                        </tasksof>
                        <id>2</id>
                        <title>file paperwork</title>
                    </todo>
                </todos>
                """;

        // assert the response in XML is correct
        assertEquals(SUCESS_CODE, response_xml.statusCode());
        assertThat(expected_response_xml_string).and(response_xml.body())
            .ignoreWhitespace()
            .normalizeWhitespace()
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.conditionalBuilder()
                                                                    .whenElementIsNamed("todo")
                                                                    .thenUse(ElementSelectors.byXPath("./id", ElementSelectors.byNameAndText))
                                                                    .elseUse(ElementSelectors.byName)
                                                                    .build()))
            .areSimilar();
    }

    @Test
    public void putTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {title: "random entity", doneStatus: true}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .PUT(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL))
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
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {title: "new entity", doneStatus: true}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // Get the newly created id
        HashMap<String, Object> response_map = helper.jsonStringToMap(response.body());
        String id = (String) response_map.get("id");
        // Add it to the expected response
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {title: "new entity",  description: "", doneStatus: "true"}
            """
        );
        // Updated the created object
        expected_response.put("id", id);

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(TODOS);
        expected_new_todos_list.add(response_map);

        // Remove newly created object
        HttpRequest test_request_cleanup = request.uri(new URI(baseURL + "/" + id))
                                    .DELETE()
                                    .build();
        client.send(test_request_cleanup, BodyHandlers.ofString());

        // assert the new object is correct
        assertEquals(CREATED_CODE, response.statusCode());
        assertEquals(expected_response, response_map);

        // assert the object exists and there is no side-effect
        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
                    (List) helper.jsonStringToMap(response_side_effect_after.body()).get(TODOS)));

        
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void postMalformedJSONTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {title: "new entity",}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL))
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
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(TODOS);

        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
                    (List) helper.jsonStringToMap(response_side_effect_after.body()).get(TODOS)));
    }


    @SuppressWarnings({ "rawtypes" })
    @Test
    public void postmalformedXMLTestFail() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

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

        HttpRequest test_request = request.uri(new URI(baseURL))
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
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL))
                .GET()
                .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(TODOS);

        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
        (List) helper.jsonStringToMap(response_side_effect_after.body()).get(TODOS)));

    }

    @Test
    public void postBadFieldTypesTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {title: "random entity", doneStatus: "string", description: false}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {"errorMessages":["Failed Validation: doneStatus should be BOOLEAN"]}
            """
        );
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void postMissingFieldsTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {doneStatus: false, description: "desc"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {"errorMessages":["title : field is mandatory"]}
            """
        );
        assertEquals(BAD_REQUEST_CODE, response.statusCode());
        assertEquals(expected_response, helper.jsonStringToMap(response.body()));

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void deleteTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .DELETE()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void optionsTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals("", response.body());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
        }

        @Test
        public void patchTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: 1, title: "random entity", doneStatus: true}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL))
                                    .method("PATCH", HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL))
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
