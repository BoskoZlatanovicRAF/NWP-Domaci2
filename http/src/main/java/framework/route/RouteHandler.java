package framework.route;

import framework.di.DIEngine;
import framework.request.Request;
import framework.request.enums.Method;
import framework.request.exceptions.RequestNotValidException;
import framework.response.Response;

import java.util.HashMap;
import java.util.Map;

public class RouteHandler {
    private final Map<String, Route> routes;
    private final DIEngine diEngine;

    public RouteHandler(DIEngine diEngine) {
        this.routes = new HashMap<>();
        this.diEngine = diEngine;
    }

    public void handleRoutes(String path, Method method, Class<?> controllerClass, java.lang.reflect.Method controllerMethod) {
        String key = method + ":" + path;
        System.out.println("Registrovana ruta: " + key);
        routes.put(key, new Route(path, method, controllerClass, controllerMethod));
    }

    public Response handleRequest(Request request) throws Exception {
        String requestPath = request.getLocation();
        Method requestMethod = request.getMethod();

        // Prvo probamo exact match
        String exactKey = requestMethod + ":" + requestPath;
        Route route = routes.get(exactKey);

        // Ako nema exact match, tražimo parametarsku rutu
        if (route == null) {
            route = findParameterizedRoute(requestMethod, requestPath);
        }

        if (route == null) {
            throw new RequestNotValidException("Ruta nije pronađena: " + exactKey);
        }

        // Dobavi instancu kontrolera kroz DI
        Object controller = diEngine.getController(route.getControllerClass());

        // Pozovi odgovarajuću metodu kontrolera
        return (Response) route.getControllerMethod().invoke(controller, request);
    }

    private Route findParameterizedRoute(Method requestMethod, String requestPath) {
        String[] requestParts = requestPath.split("/");

        for (Map.Entry<String, Route> entry : routes.entrySet()) {
            Route route = entry.getValue();
            String routePath = route.getPath();
            String[] routeParts = routePath.split("/");

            if (route.getMethod() == requestMethod &&
                    routeParts.length == requestParts.length) {
                boolean matches = true;
                for (int i = 0; i < routeParts.length; i++) {
                    if (!routeParts[i].equals(requestParts[i]) &&
                            !routeParts[i].matches("\\{.*\\}")) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return route;
                }
            }
        }
        return null;
    }
}