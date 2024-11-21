package dev.mccue.jdk.httpserver.rutrouter;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RutRouterTest {
    @Test
    public void basicRoutingTest() {

    }

    public static void main(String[] args) throws Exception {
        var router = RutRouter.builder()
                .get("/a/b/<c>", e -> {
                    e.getResponseHeaders().put("Content-Type", List.of("text/html"));
                    e.sendResponseHeaders(200, 0);
                    var c = RutRouter.result(e);
                    try (var body = e.getResponseBody()) {
                        body.write(("<h1>Hiya  " + c.paramValueDecoded(0) + "   " + c.query() + "</h1>").getBytes(StandardCharsets.UTF_8));
                    }
                })
                .notFoundHandler(e -> {
                    e.getResponseHeaders().put("Content-Type", List.of("text/html"));
                    e.sendResponseHeaders(404, 0);
                    try (var body = e.getResponseBody()) {
                        body.write("<h1>Not Found</h1>".getBytes(StandardCharsets.UTF_8));
                    }
                })
                .build();

        var server = HttpServer.create(new InetSocketAddress(8783), 0);
        server.createContext("/", router);
        server.start();
    }
}
