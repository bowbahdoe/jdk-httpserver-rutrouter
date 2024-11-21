# jdk-httpserver-rutrouter

Router for the JDK's httpserver API based on [io.norberg.rut](https://github.com/danielnorberg/rut)

```xml 
<dependency>
    <groupId>dev.mccue</groupId>
    <artifactId>jdk-httpserver-rutrouter</artifactId>
    <version>2024.11.21</version>
</dependency>
```

## Usage

For more specific information on how to structure routes, refer to the [io.norberg.rut](https://github.com/danielnorberg/rut)
examples and documentation.

```java
import module dev.mccue.jdk.httpserver.rutrouter;

void main() {
    var router = RutRouter.builder()
            .get("/hello/<name>", e -> {
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

```