package dev.notevault;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/** Minimalna „przeglądarka”: trzyma ciasteczka i odsyła token CSRF tak jak app.js. */
class Browser {

	private final String base;
	private final CookieManager cookies = new CookieManager();
	private final HttpClient http;

	Browser(int port) {
		this.base = "http://localhost:" + port;
		this.http = HttpClient.newBuilder().cookieHandler(cookies).build();
	}

	String cookie(String name) {
		return cookies.getCookieStore().getCookies().stream()
			.filter(c -> c.getName().equals(name))
			.map(HttpCookie::getValue)
			.findFirst()
			.orElse(null);
	}

	HttpResponse<String> get(String path) throws IOException, InterruptedException {
		return http.send(HttpRequest.newBuilder(URI.create(base + path)).build(), BodyHandlers.ofString());
	}

	HttpResponse<String> login(String username, String password) throws IOException, InterruptedException {
		get("/api/csrf");
		return post("/api/login", "username=" + username + "&password=" + password,
				"application/x-www-form-urlencoded");
	}

	HttpResponse<String> post(String path, String body, String contentType)
			throws IOException, InterruptedException {
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path))
			.header("Content-Type", contentType)
			.POST(BodyPublishers.ofString(body));
		String token = cookie("XSRF-TOKEN");
		if (token != null) {
			request.header("X-XSRF-TOKEN", token);
		}
		return http.send(request.build(), BodyHandlers.ofString());
	}

}
