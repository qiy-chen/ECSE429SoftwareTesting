import org.apache.commons.collections.CollectionUtils;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*; 
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;

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
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import javax.xml.parsers.ParserConfigurationException;

@TestMethodOrder(MethodOrderer.Random.class)
public class todoTasksOfTest {
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
    public void getAllTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Set content to XML
        request.setHeader("Content-Type", "application/xml")
                .setHeader("Accept", "application/xml");
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        
        HttpResponse<String> response_xml = client.send(test_request, BodyHandlers.ofString());
        // Set content to JSON
        request.setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");
        String expected_response_xml_string = String.format("""
                <projects>
                    <project>
                        <active>false</active>
                        <description />
                        <id>1</id>
                        <completed>false</completed>
                        <title>Office Work</title>
                        <tasks>
                            <id>%s</id>
                        </tasks>
                        <tasks>
                            <id>2</id>
                        </tasks>
                        <tasks>
                            <id>1</id>
                        </tasks>
                    </project>
                </projects>
                """, test_instance.get("id"));
        System.out.println(response_xml.body());
        System.out.println(expected_response_xml_string);
        // assert the response in XML is correct
        assertEquals(SUCESS_CODE, response_xml.statusCode());
        assertThat(expected_response_xml_string).and(response_xml.body())
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
    public void putTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "1"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void postTestSuccess() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = String.format(
            """
                {id: "%s"}
                """
            , test_second_project_id);
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // Get added category data
        HttpRequest added_category = request.uri(new URI(projectsURL + "/" + test_second_project_id))
                                    .GET()
                                    .build();
        HttpResponse<String> response_added_category = client.send(added_category, BodyHandlers.ofString());
        System.out.println(response_added_category.body());

        // After the request state
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(PROJECTS);
        expected_new_todos_list.add(helper.getFirstInstanceFromListFromProp(helper.jsonStringToMap(response_added_category.body()), PROJECTS));

        // Remove newly created relation
        HttpRequest test_request_cleanup = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof/" + test_second_project_id))
                                    .DELETE()
                                    .build();
        client.send(test_request_cleanup, BodyHandlers.ofString());

        // assert the new relation is correct
        assertEquals(CREATED_CODE, response.statusCode());

        // assert the relations are correct
        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
                    (List) helper.jsonStringToMap(response_side_effect_after.body()).get(PROJECTS)));

        
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void postMalformedJSONTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "2",}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
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
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(PROJECTS);

        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
                    (List) helper.jsonStringToMap(response_side_effect_after.body()).get(PROJECTS)));
    }


    @SuppressWarnings({ "rawtypes" })
    @Test
    public void postmalformedXMLTestFail() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                <
                """;

        // Set content to XML
        request.setHeader("Content-Type", "application/xml")
                .setHeader("Accept", "application/xml");

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
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
        HttpRequest test_request_side_effect_after = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                .GET()
                .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect_after, BodyHandlers.ofString());
        List expected_new_todos_list = (List) helper.jsonStringToMap(response_side_effect_before.body()).get(PROJECTS);

        assertTrue(CollectionUtils.isEqualCollection(expected_new_todos_list, 
        (List) helper.jsonStringToMap(response_side_effect_after.body()).get(PROJECTS)));

    }

    @Test
    public void postNonExistentTestFail() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: "-1"}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        HashMap<String, Object> expected_response = helper.jsonStringToMap(
            """
            {errorMessages: ["Could not find thing matching value for id"]}
            """
        );

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
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
    public void deleteTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .DELETE()
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
    public void optionsTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());
        
        assertEquals(SUCESS_CODE, response.statusCode());
        assertEquals("", response.body());

        // Check if no side-effect after the request
        HttpRequest test_request_side_effect = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_after = client.send(test_request_side_effect, BodyHandlers.ofString());
        assertEquals(helper.jsonStringToMap(response_side_effect_before.body()), 
                    helper.jsonStringToMap(response_side_effect_after.body()));
        }

        @Test
        public void patchTestNotAllowed() throws IOException, InterruptedException, URISyntaxException{
        // Before the request state
        HttpRequest test_request_side_effect_before = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .GET()
                                    .build();
        HttpResponse<String> response_side_effect_before = client.send(test_request_side_effect_before, BodyHandlers.ofString());

        String request_body = """
                {id: 1, title: "random entity", doneStatus: true}
                """;
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .method("PATCH", HttpRequest.BodyPublishers.ofString(request_body))
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
    public void headTestSuccess() throws IOException, InterruptedException, URISyntaxException, SAXException, ParserConfigurationException{
        HttpRequest test_request = request.uri(new URI(baseURL + "/" + test_instance.get("id") + "/tasksof" ))
                                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                    .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        // assert the response in JSON is correct
        assertEquals(SUCESS_CODE, response.statusCode());
        assertNotEquals(null, response.headers());
        assertEquals("", response.body());
    }
}
