import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.TimeUnit;

class Script {
    static final String baseURL = "http://localhost:4567/";
    static HttpClient client = HttpClient.newHttpClient();
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        System.out.println("Hello");
        // /todos endpoint
        System.out.println("Sending request to /todos");
        Builder request = HttpRequest.newBuilder(new URI(baseURL + "todos"));
        options(request);
        head(request);
        get(request);
        post(request, 
        """
        {
            "title": "olor in reprehenderi",
            "doneStatus": false,
            "description": "irure dolor in repra"
        }
                """
        );
        // /todoes/:id endpoint
        System.out.println("Sending request to /todos/1");
        Builder request2 = HttpRequest.newBuilder(new URI(baseURL + "todos/1"));
        options(request2);
        head(request2);
        get(request2);
        post(request2,
        """
        {
            "title": "posttest"
        }
                """
        );
        put(request2,
        """
        {
            "title": "puttest"
        }
                """
        );
        System.out.println("Sending request to /todos/2");
        Builder request3 = HttpRequest.newBuilder(new URI(baseURL + "todos/2"));
        delete(request3);
        //getDocs();
        // /todos/:id/categories endpoint
        System.out.println("Sending request to /todos/1/categories");
        Builder request4 = HttpRequest.newBuilder(new URI(baseURL + "todos/1/categories"));
        options(request4);
        head(request4);
        get(request4);
        post(request4,
        """
        {
            "id": "2"
        }
                """
        );
        System.out.println("Sending request to /todos/1/categories/2");
        Builder request5 = HttpRequest.newBuilder(new URI(baseURL + "todos/1/categories/2"));
        System.out.println("Before delete: ");
        get(request4);
        delete(request5);
        System.out.println("After delete: ");
        get(request4);
        System.out.println("Sending request to /todos/1/tasksof");
        Builder request6 = HttpRequest.newBuilder(new URI(baseURL + "todos/1/tasksof"));
        options(request6);
        head(request6);
        get(request6);
        post(request6,
        """
        {
            "id": "1"
        }
                """
        );
        System.out.println("Sending request to /todos/1/tasksof/1");
        Builder request7 = HttpRequest.newBuilder(new URI(baseURL + "todos/1/tasksof/1"));
        System.out.println("Before delete: ");
        get(request6);
        delete(request7);
        System.out.println("After delete: ");
        get(request6);
        TimeUnit.SECONDS.sleep(10);
        shutdown();
    }

    public static void options(Builder request) throws IOException, InterruptedException{
        request.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
        System.out.println("-- OPTIONS --");
        HttpRequest req = request.build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        printResponse(res);
    }

    public static void head(Builder request) throws IOException, InterruptedException{
        request.method("HEAD", HttpRequest.BodyPublishers.noBody());
        System.out.println("-- HEAD --");
        HttpRequest req = request.build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        printResponse(res);
        System.out.println("Headers: " + res.headers());
    }

    public static void get(Builder request) throws IOException, InterruptedException{
        request.GET();
        System.out.println("-- GET --");
        HttpRequest req = request.build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        printResponse(res);
    }

    public static void post(Builder request, String jsonString) throws IOException, InterruptedException{
        System.out.println("-- POST --");
        System.out.println("Request Body: " + jsonString);
        System.out.println("=================");
        System.out.println("Before:");
        get(request);
        System.out.println("=================");
        request.POST(HttpRequest.BodyPublishers.ofString(jsonString));
        System.out.println("Response Body: ");
        HttpRequest req = request.build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        printResponse(res);
        System.out.println("=================");
        System.out.println("After:");
        get(request);
        System.out.println("=================");
    }

    public static void put(Builder request, String jsonString) throws IOException, InterruptedException{
        System.out.println("-- PUT --");
        System.out.println("Request Body: " + jsonString);
        System.out.println("=================");
        System.out.println("Before:");
        get(request);
        System.out.println("=================");
        request.PUT(HttpRequest.BodyPublishers.ofString(jsonString));
        System.out.println("Response Body: ");
        HttpRequest req = request.build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        printResponse(res);
        System.out.println("=================");
        System.out.println("After:");
        get(request);
        System.out.println("=================");
    }

    public static void delete(Builder request) throws IOException, InterruptedException{
        System.out.println("-- DELETE --");
        System.out.println("=================");
        System.out.println("Before:");
        get(request);
        System.out.println("=================");
        request.DELETE();
        HttpRequest req = request.build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        printResponse(res);
        System.out.println("=================");
        System.out.println("After:");
        get(request);
        System.out.println("=================");
    }

    public static void getDocs() throws IOException, InterruptedException, URISyntaxException{
        Builder request = HttpRequest.newBuilder(new URI(baseURL + "docs"));
        request.GET();
        System.out.println("GET /docs");
        HttpRequest req = request.build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        printResponse(res);
    }

    public static void shutdown() throws IOException, InterruptedException, URISyntaxException{
        Builder request = HttpRequest.newBuilder(new URI(baseURL + "shutdown"));
        request.GET();
        System.out.println("GET /shutdown");
        HttpRequest req = request.build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        printResponse(res);
    }

    public static void printResponse(HttpResponse<String> res){
        System.out.println("Status Code: " + res.statusCode());
        System.out.println("Body: " + res.body());
    }
}
