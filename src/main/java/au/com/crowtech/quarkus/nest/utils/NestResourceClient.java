package au.com.crowtech.quarkus.nest.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.crowtech.quarkus.nest.models.GennyToken;

@ApplicationScoped
public class NestResourceClient {
	private final ExecutorService executorService = Executors.newFixedThreadPool(5);
	private final HttpClient httpClient = HttpClient.newBuilder().executor(executorService)
			.version(HttpClient.Version.HTTP_2).build();

	public NestResourceClient() {
	}

	public HttpResponse<String> get(String uri, GennyToken token) throws Exception {
	    HttpClient client = HttpClient.newBuilder().build();
	    HttpRequest request = HttpRequest.newBuilder()
	    		.version(HttpClient.Version.HTTP_2)
	    		.header("Content-Type", "application/json")
	    		.header("Authorization", "Bearer "+token.getToken())
	            .uri(URI.create(uri))
	            .GET()
	            .build();

	 //   HttpResponse<?> response = client.send(request, BodyHandlers.discarding());
	 //   HttpResponse<?> response = client.send(request,BodyHandlers.ofString());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
  	}
	
	
	public HttpResponse<String> post(String uri, GennyToken token, String data) throws Exception {
	    HttpClient client = HttpClient.newBuilder().build();
	    HttpRequest request = HttpRequest.newBuilder()
	    		.version(HttpClient.Version.HTTP_2)
	    		.header("Content-Type", "application/json")
	    		.header("Authorization", "Bearer "+token.getToken())
	            .uri(URI.create(uri))
	            .POST(BodyPublishers.ofString(data))
	            .build();

	 //   HttpResponse<?> response = client.send(request, BodyHandlers.discarding());
	 //   HttpResponse<?> response = client.send(request,BodyHandlers.ofString());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
  	}
	
	public HttpResponse<String> put(String uri, GennyToken token, String data) throws Exception {
	    HttpClient client = HttpClient.newBuilder().build();
	    HttpRequest request = HttpRequest.newBuilder()
	    		.version(HttpClient.Version.HTTP_2)
	    		.header("Content-Type", "application/json")
	    		.header("Authorization", "Bearer "+token.getToken())
	            .uri(URI.create(uri))
	            .PUT(BodyPublishers.ofString(data))
	            .build();

	 //   HttpResponse<?> response = client.send(request, BodyHandlers.discarding());
	 //   HttpResponse<?> response = client.send(request,BodyHandlers.ofString());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        return response;
  	}
	public CompletableFuture<Void> postJSON(URI uri, Map<String, String> map) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);

		HttpRequest request = HttpRequest.newBuilder(uri).header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(requestBody)).build();

		return HttpClient.newHttpClient().sendAsync(request, BodyHandlers.ofString())
				.thenApply(HttpResponse::statusCode).thenAccept(System.out::println);
	}

//    CompletionStage<String> createUser(String q) {
//        return  this.httpClient
//                 .sendAsync(
//                         HttpRequest.newBuilder()
//                                 .POST()
//                                 .uri(URI.create("https://localhost:8080/posts/count?q=" + q))
//                                 .header("Accept", "application/json")
//                                 .build()
//                         ,
//                         HttpResponse.BodyHandlers.ofString()
//                 )
//                 .thenApply(HttpResponse::body)
//       //          .thenApply(String.class)
//                 .toCompletableFuture();
//     } 

//    CompletionStage<Long> countAllPosts(String q) {
//       return  this.httpClient
//                .sendAsync(
//                        HttpRequest.newBuilder()
//                                .GET()
//                                .uri(URI.create("http://localhost:8080/posts/count?q=" + q))
//                                .header("Accept", "application/json")
//                                .build()
//                        ,
//                        HttpResponse.BodyHandlers.ofString()
//                )
//                .thenApply(HttpResponse::body)
//                .thenApply(Long::parseLong)
//                .toCompletableFuture();
//    }
//    CompletionStage<List<Post>> getAllPosts(
//            String q,
//            int offset,
//            int limit
//    ) {
//        return  this.httpClient
//                .sendAsync(
//                        HttpRequest.newBuilder()
//                                .GET()
//                                .uri(URI.create("http://localhost:8080/posts?q=" + q + "&offset=" + offset + "&limit=" + limit))
//                                .header("Accept", "application/json")
//                                .build()
//                        ,
//                        HttpResponse.BodyHandlers.ofString()
//                )
//                .thenApply(HttpResponse::body)
//                .thenApply(stringHttpResponse -> JsonbBuilder.newBuilder().build().fromJson(stringHttpResponse, new TypeLiteral<List<Post>>() {}.getType()))
//                .thenApply(data ->(List<Post>)data)
//                .toCompletableFuture();
//    }
}