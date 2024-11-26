package com.testproject;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import com.sun.management.OperatingSystemMXBean;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@TestMethodOrder(MethodOrderer.Random.class)
public class perf_todosCategoriesTest {
    static final String URLTodos = "http://localhost:4567/todos";
    static final String URLCategories = "http://localhost:4567/categories";
    static HttpClient client = HttpClient.newHttpClient();
    static Builder request = HttpRequest.newBuilder();
    static HashMap<String, Object> test_instance = null;

    static final int SUCESS_CODE = 200;
    static final int CREATED_CODE = 201;
    static final int BAD_REQUEST_CODE = 400; 
    static final int NOT_FOUND_CODE = 404; 
    static final int NOT_ALLOWED_CODE = 405;

    static final int[] nb_objects_tested = {50, 100, 1000, 2500, 5000};

    static final String TODOS = "todos";
    static final String CATEGORIES = "categories";
    
    @BeforeEach
    public void startup() throws Throwable{
        try{
            HttpRequest init_ping_request = request.uri(new URI(URLTodos)).build();
            client.send(init_ping_request, BodyHandlers.ofString());
            // Get to a clean state
            cleanup();

            // Create test instance
            String request_test_body = """
            {title: "test entity", doneStatus: false, description: "desc"}
            """;
            HttpRequest test_instance_request = request.uri(new URI(URLTodos))
                                        .POST(HttpRequest.BodyPublishers.ofString(request_test_body))
                                        .build();
            HttpResponse<String> response_todo = client.send(test_instance_request, BodyHandlers.ofString());
            HashMap<String, Object> response_todo_test_instance = helper.jsonStringToMap(response_todo.body());
         
            // Set the test instances
            test_instance = response_todo_test_instance;
    }
    catch (Throwable  e){
        System.out.println(e);
        throw e;
    }
    }

    @AfterEach
    public void cleanup() throws Throwable{
        // Cleanup todos
        HttpRequest test_request = request.uri(new URI(URLTodos))
                                        .GET()
                                        .build();
        HttpResponse<String> response = client.send(test_request, BodyHandlers.ofString());

        @SuppressWarnings({ "unchecked" })
        List<LinkedTreeMap<String, Object>> todos_list= (List<LinkedTreeMap<String, Object>>) helper.jsonStringToMap(response.body()).get(TODOS);
        // Cleanup all instances of todos
        for (int i = 0; i < todos_list.size(); i++){
            // Convert to hashmap
            @SuppressWarnings("unchecked")
            HashMap<String, Object> instance = new Gson().fromJson(new Gson().toJson(((LinkedTreeMap<String, Object>) todos_list.get(i))), HashMap.class);
            HttpRequest delete_request = request.uri(new URI(URLTodos + "/" + (String) instance.get("id") ))
                                .DELETE()
                                .build();
            client.send(delete_request, BodyHandlers.ofString());
        }

        // Cleanup categories
        HttpRequest test_request_cat = request.uri(new URI(URLCategories))
                                        .GET()
                                        .build();
        HttpResponse<String> response_cat = client.send(test_request_cat, BodyHandlers.ofString());
        @SuppressWarnings({ "unchecked" })
        List<LinkedTreeMap<String, Object>> cat_list= (List<LinkedTreeMap<String, Object>>) helper.jsonStringToMap(response_cat.body()).get(CATEGORIES);
        // Cleanup all instances of categories
        for (int i = 0; i < cat_list.size(); i++){
            // Convert to hashmap
            @SuppressWarnings("unchecked")
            HashMap<String, Object> instance = new Gson().fromJson(new Gson().toJson(((LinkedTreeMap<String, Object>) cat_list.get(i))), HashMap.class);
            HttpRequest delete_request = request.uri(new URI(URLCategories + "/" + (String) instance.get("id") ))
                                .DELETE()
                                .build();
            client.send(delete_request, BodyHandlers.ofString());
        }
    }

    @Test
    public void addCategoryRelationTestPerf() throws Throwable{
        OperatingSystemMXBean operatingSystemMXBean = 
          (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        for (int i = 0; i < nb_objects_tested.length; i++){
            // Populate server
            LinkedList<String> list_ids = new LinkedList<String>();
            String request_body = """
                    {title: "new cat"}
                    """;
            HttpRequest populate_request = request.uri(new URI(URLCategories))
                                            .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                            .build();
            for (int j = 0; j < nb_objects_tested[i]; j++){
                HttpResponse<String> response_creation = client.send(populate_request, BodyHandlers.ofString());
                // Add new instance to ids list
                list_ids.add((String) helper.jsonStringToMap(response_creation.body()).get("id"));
            }
            // Wait for server to be idle
            Thread.sleep(3000);

            // Cpu usage stats
            LinkedList<Double> cpu_usage = new LinkedList<Double>();
            // Free memory stats
            LinkedList<Long> free_memory = new LinkedList<Long>();
            // Test create time
            LinkedList<Long> time = new LinkedList<Long>();
            for (int j = 0; j < nb_objects_tested[i]; j ++){
                HashMap<String, Object> new_request_body = new HashMap<String, Object>();
                new_request_body.put("id", list_ids.get(j));
                HttpRequest test_request = request.uri(new URI(URLTodos + "/" + test_instance.get("id") + "/categories"))
                                            .POST(HttpRequest.BodyPublishers.ofString(helper.mapToJSONString(new_request_body)))
                                            .build();
                long create_start_time = System.currentTimeMillis();
                client.send(test_request, BodyHandlers.ofString());
                long create_end_time = System.currentTimeMillis();
                // Record cpu and memory stats
                cpu_usage.add(operatingSystemMXBean.getProcessCpuLoad());
                free_memory.add(operatingSystemMXBean.getFreeMemorySize());
                time.add(create_end_time - create_start_time);
                }
            
            System.out.println(String.format("ADD %d CATEGORY RELATIONS:\t %d ms\t %f CPU \t %d Free Memory", nb_objects_tested[i], helper.getSumListLong(time), helper.getMeanListDouble(cpu_usage), helper.getMeanListLong(free_memory)));
            // Wait for server to be idle
            Thread.sleep(3000);
        }
        
    }

    @Test
    public void deleteCategoryRelationsTestPerf() throws Throwable {
        OperatingSystemMXBean operatingSystemMXBean = 
          (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        for (int i = 0; i < nb_objects_tested.length; i++){
            // Populate server
            LinkedList<String> list_ids = new LinkedList<String>();
            String request_body = """
                    {title: "new cat"}
                    """;
            HttpRequest populate_request = request.uri(new URI(URLCategories))
                                            .POST(HttpRequest.BodyPublishers.ofString(request_body))
                                            .build();
            for (int j = 0; j < nb_objects_tested[i]; j++){
                HttpResponse<String> response_creation = client.send(populate_request, BodyHandlers.ofString());
                // Add new instance to ids list
                list_ids.add((String) helper.jsonStringToMap(response_creation.body()).get("id"));
            }
            // Add relations
            for (int j = 0; j < list_ids.size(); j ++){
                HashMap<String, Object> new_request_body = new HashMap<String, Object>();
                new_request_body.put("id", list_ids.get(j));
                HttpRequest test_request = request.uri(new URI(URLTodos + "/" + test_instance.get("id") + "/categories"))
                                            .POST(HttpRequest.BodyPublishers.ofString(helper.mapToJSONString(new_request_body)))
                                            .build();
                client.send(test_request, BodyHandlers.ofString());
                }

            // Wait for server to be idle
            Thread.sleep(3000);

            // Delete instance of todos relations
            // Cpu usage stats
            LinkedList<Double> cpu_usage_delete = new LinkedList<Double>();
            // Free memory stats
            LinkedList<Long> free_memory_delete = new LinkedList<Long>();
            // Test delete time
            LinkedList<Long> time = new LinkedList<Long>();

            for (int j = 0; j < list_ids.size(); j ++){
                HashMap<String, Object> new_request_body = new HashMap<String, Object>();
                new_request_body.put("id", list_ids.get(j));
                HttpRequest test_request = request.uri(new URI(URLTodos + "/" + test_instance.get("id") + "/categories/" + list_ids.get(j)))
                                            .DELETE()
                                            .build();
                long delete_start_time = System.currentTimeMillis();
                client.send(test_request, BodyHandlers.ofString());
                long delete_end_time = System.currentTimeMillis();
                // Record cpu and memory stats
                cpu_usage_delete.add(operatingSystemMXBean.getProcessCpuLoad());
                free_memory_delete.add(operatingSystemMXBean.getFreeMemorySize());
                time.add(delete_end_time - delete_start_time);
                }
            
            System.out.println(String.format("DELETE %d CATEGORY RELATIONS:\t %d ms\t %f CPU \t %d Free Memory", nb_objects_tested[i], helper.getSumListLong(time), helper.getMeanListDouble(cpu_usage_delete), helper.getMeanListLong(free_memory_delete)));
            // Wait for server to be idle
            Thread.sleep(3000);
        }
    }
}