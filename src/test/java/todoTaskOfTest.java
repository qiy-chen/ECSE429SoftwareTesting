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
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.xml.sax.SAXException;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

@TestMethodOrder(MethodOrderer.Random.class)
public class todoTaskOfTest {
    static final String baseURL = "http://localhost:4567/todos";
    static final String projectsURL = "http://localhost:4567/projects";
    static HashMap<String, Object> test_instance = null;
    static HashMap<String, Object> test_tasksof_related_instance = null;
    static String test_second_project_id = null;
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();

    static final int SUCESS_CODE = 200;
    static final int CREATED_CODE = 201;
    static final int BAD_REQUEST_CODE = 400; 
    static final int NOT_FOUND_CODE = 404; 
    static final int NOT_ALLOWED_CODE = 405; 

    static final String TODOS = "todos";
    static final String PROJECTS = "projects";

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
            String request_taskof_relation_test_body = """
            {id: "1"}
            """;
            HttpRequest test_instance_request = request.uri(new URI(baseURL))
                                        .POST(HttpRequest.BodyPublishers.ofString(request_test_body))
                                        .build();
            HttpResponse<String> response_todo = client.send(test_instance_request, BodyHandlers.ofString());
            HashMap<String, Object> response_todo_test_instance = helper.jsonStringToMap(response_todo.body());

            HttpRequest test_instance_add_tasksoff_request = request.uri(new URI(baseURL + "/" + response_todo_test_instance.get("id") + "/tasksof") )
                                        .POST(HttpRequest.BodyPublishers.ofString(request_taskof_relation_test_body))
                                        .build();
            client.send(test_instance_add_tasksoff_request, BodyHandlers.ofString());

            // Create a second project
            String request_test_second_project = """
            {title: "Second Project", completed: false, active: false}
            """;
            HttpRequest test_instance_request_second_project = request.uri(new URI(projectsURL))
                                        .POST(HttpRequest.BodyPublishers.ofString(request_test_second_project))
                                        .build();
            HttpResponse<String> response_second_project = client.send(test_instance_request_second_project, BodyHandlers.ofString());
            test_second_project_id = (String) helper.jsonStringToMap(response_second_project.body()).get("id");
            // Set the test instances
            test_instance = response_todo_test_instance;
            test_tasksof_related_instance = helper.jsonStringToMap(String.format(
                """
                {active: false, description: "", id: 1, completed: "false", title: "Office Work"}
                """
            , response_todo_test_instance.get("id")));
            
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

            HttpRequest test_instance_request_second_project = request.uri(new URI(projectsURL + "/" + test_second_project_id ))
                                        .DELETE()
                                        .build();
            client.send(test_instance_request_second_project, BodyHandlers.ofString());
            // Set the test instance
            test_instance = null;
            test_tasksof_related_instance = null;
        }
        catch (Throwable  e){
            System.out.println(e);
            throw e;
        }
    }

    @Test
    public void getTestNotAllowedAsExpected() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the response in JSON is correct
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());
    }

    @Test
    public void getTestNotAllowedActual() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the response in JSON is correct
        assertEquals(NOT_FOUND_CODE, response.statusCode());
    }

    @Test
    public void putTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "1"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .PUT(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void postTestNotAllowedAsExpected() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "1"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void postTestNotAllowedActual() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "1"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_FOUND_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void deleteTestSuccess() throws IOException, InterruptedException, URISyntaxException, ParserConfigurationException, SAXException{
        // Receive XML payloads
        request.setHeader("Accept", "application/xml");

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id")))
                                    .DELETE()
                                    .build();
        HttpResponse<String> response_xml = client.send(test_request, BodyHandlers.ofString());

        // assert the returned status is correct
        assertEquals(SUCESS_CODE, response_xml.statusCode());

        // Check if no side-effect after the request (all instances endpoint)
        HttpRequest test_request_side_effect_all_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof"))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_after_xml = client.send(test_request_side_effect_all_after, BodyHandlers.ofString());

        // Switch to JSON payloads
        request.setHeader("Accept", "application/json");

        String expected_response_side_effect_all_after_xml_string = String.format(
            """
                <projects>
                </projects>
                """
        , test_tasksof_related_instance.get("id"));
        // assert the object is removed from the list and there is no side-effect (aka return to initial state)
        assertThat(expected_response_side_effect_all_after_xml_string).and(response_side_effect_all_after_xml.body())
            .ignoreWhitespace()
            .normalizeWhitespace()
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.conditionalBuilder()
                                                                    .whenElementIsNamed("tasks")
                                                                    .thenUse(ElementSelectors.byXPath("./id", ElementSelectors.byNameAndText))
                                                                    .elseUse(ElementSelectors.byName)
                                                                    .build()))
            .areSimilar();
        }

    @Test
    public void deleteSecondTimeTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Receive XML payloads
        request.setHeader("Accept", "application/xml");

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id")))
                                    .DELETE()
                                    .build();
        HttpResponse<String> response_xml = client.send(test_request, BodyHandlers.ofString());
        HttpResponse<String> response_second_delete_xml = client.send(test_request, BodyHandlers.ofString());

        // assert the returned status is correct
        assertEquals(SUCESS_CODE, response_xml.statusCode());
        assertEquals(NOT_FOUND_CODE, response_second_delete_xml.statusCode());

        // Check if no side-effect after the request (all instances endpoint)
        HttpRequest test_request_side_effect_all_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof"))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_after_xml = client.send(test_request_side_effect_all_after, BodyHandlers.ofString());

        
        // Switch to JSON payloads
        request.setHeader("Accept", "application/json");

        String expected_response_side_effect_all_after_xml_string = String.format(
            """
                <projects>
                </projects>
                """
        , test_tasksof_related_instance.get("id"));
        // assert the object is removed from the list and there is no side-effect (aka return to initial state)
        assertThat(expected_response_side_effect_all_after_xml_string).and(response_side_effect_all_after_xml.body())
            .ignoreWhitespace()
            .normalizeWhitespace()
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.conditionalBuilder()
                                                                    .whenElementIsNamed("tasks")
                                                                    .thenUse(ElementSelectors.byXPath("./id", ElementSelectors.byNameAndText))
                                                                    .elseUse(ElementSelectors.byName)
                                                                    .build()))
            .areSimilar();
        }

        @SuppressWarnings({"rawtypes" })
    @Test
    public void deleteNonExistentTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_all_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof"))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_before = client.send(test_request_side_effect_all_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/-1"))
                                    .DELETE()
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the returned status is correct
        assertEquals(NOT_FOUND_CODE, response.statusCode());

        // Check if no side-effect after the request (all instances endpoint)
        HttpRequest test_request_side_effect_all_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof"))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_all_after = client.send(test_request_side_effect_all_after, BodyHandlers.ofString());
        List expected_initial_projects_list = (List) helper.jsonStringToMap(response_side_effect_all_after.body()).get(PROJECTS);

        // assert the object is removed from the list and there is no side-effect
        assertTrue(CollectionUtils.isEqualCollection((List) helper.jsonStringToMap(response_side_effect_all_before.body()).get(PROJECTS), 
                                expected_initial_projects_list));
        }

    @Test
    public void optionsTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals("", response.body());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
        }

    @Test
    public void patchTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: 1, title: "random entity", doneStatus: true}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .method("PATCH", HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
    }

    @Test
    public void headTestNotAllowedAsExpected() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the response in JSON is correct
        assertEquals(NOT_ALLOWED_CODE, response.statusCode());
        assertNotEquals(null, response.headers());
        assertEquals("", response.body());
    }

    @Test
    public void headTestNotAllowedActual() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_tasksof_related_instance.get("id") ))
                                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the response in JSON is correct
        assertEquals(NOT_FOUND_CODE, response.statusCode());
        assertNotEquals(null, response.headers());
        assertEquals("", response.body());
    }
}
