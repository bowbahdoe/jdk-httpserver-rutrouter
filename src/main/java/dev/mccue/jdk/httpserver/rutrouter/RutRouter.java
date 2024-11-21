package dev.mccue.jdk.httpserver.rutrouter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.norberg.rut.Router;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;


public final class RutRouter implements HttpHandler {
    private final Router<HttpHandler> router;

    private final ErrorHandler errorHandler;
    private final HttpHandler notFoundHandler;

    private static final String ROUTE_MATCH_KEY
            = "dev.mccue.jdk.httpserver.rutrouter/router-result";

    private RutRouter(Builder builder) {
        this.router = builder.routerBuilder.build();
        this.errorHandler = builder.errorHandler;
        this.notFoundHandler = builder.notFoundHandler;
    }

    public static Router.Result<?> result(HttpExchange httpExchange) {
        return (Router.Result<?>) httpExchange.getAttribute(ROUTE_MATCH_KEY);
    }

    /**
     * Creates a {@link Builder}.
     *
     * @return A builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Handles the request if there is a matching handler.
     * @param exchange The request to handle.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var result = router.result();
        router.route(exchange.getRequestMethod(), exchange.getRequestURI().toString(), result);

        exchange.setAttribute(ROUTE_MATCH_KEY, result);
        if (result.isSuccess()) {
            try {
                result.target().handle(exchange);
            }
            catch (Throwable t) {
                errorHandler.handle(t, exchange);
            }
        }
        else {
            notFoundHandler.handle(exchange);
        }
    }

    /**
     * A builder for {@link RutRouter}.
     */
    public static final class Builder {
        private final Router.Builder<HttpHandler> routerBuilder;
        private ErrorHandler errorHandler;
        private HttpHandler notFoundHandler;

        private Builder() {
            this.routerBuilder = Router.builder();
            this.errorHandler = new InternalErrorHandler();
            this.notFoundHandler = new NotFoundHandler();
        }

        public Builder route(
                String method,
                String routePattern,
                HttpHandler handler
        ) {
            return route(List.of(method), routePattern, handler);
        }

        public Builder route(
                List<String> methods,
                String routePattern,
                HttpHandler handler
        ) {
            Objects.requireNonNull(methods);
            Objects.requireNonNull(routePattern);
            Objects.requireNonNull(handler);

            if (!methods.isEmpty()) {
                for (var method : methods) {
                    routerBuilder.route(method.toUpperCase(), routePattern, handler);
                }
            }

            return this;
        }

        public Builder get(String routePattern, HttpHandler handler) {
            return route("get", routePattern, handler);
        }

        public Builder post(String routePattern, HttpHandler handler) {
            return route("post", routePattern, handler);
        }

        public Builder patch(String routePattern, HttpHandler handler) {
            return route("patch", routePattern, handler);
        }

        public Builder put(String routePattern, HttpHandler handler) {
            return route("put", routePattern, handler);
        }

        public Builder head(String routePattern, HttpHandler handler) {
            return route("head", routePattern, handler);
        }

        public Builder delete(String routePattern, HttpHandler handler) {
            return route("delete", routePattern, handler);
        }

        public Builder options(String routePattern, HttpHandler handler) {
            return route("options", routePattern, handler);
        }

        public Builder errorHandler(Function<Throwable, HttpHandler> errorHandler) {
            this.errorHandler = (t, exchange) -> errorHandler.apply(t).handle(exchange);
            return this;
        }

        public Builder errorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public Builder notFoundHandler(HttpHandler handler) {
            this.notFoundHandler = handler;
            return this;
        }

        public Builder optionalTrailingSlash(boolean optional) {
            this.routerBuilder.optionalTrailingSlash(optional);
            return this;
        }

        /**
         * Builds the {@link RutRouter}.
         * @return The built router, ready to handle requests.
         */
        public RutRouter build() {
            return new RutRouter(this);
        }
    }

    public interface ErrorHandler {
        void handle(Throwable error, HttpExchange exchange) throws IOException;
    }
}
