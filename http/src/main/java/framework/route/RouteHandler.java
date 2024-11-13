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

    // registruje novu rutu
    public void handleRoutes(String path, Method method, Class<?> controllerClass, java.lang.reflect.Method controllerMethod) {
        String key = method + ":" + path;

        if (routes.containsKey(key)) {
            throw new RuntimeException("Ruta već postoji: " + key);
        }

        routes.put(key, new Route(path, method, controllerClass, controllerMethod));
        System.out.println("Registrovana ruta: " + key);
    }

    // obrađuje zahtev i vraća odgovor
    public Response handleRequest(Request request) throws Exception {
        String key = request.getMethod() + ":" + request.getLocation();
        Route route = routes.get(key);

        if (route == null) {
            throw new RequestNotValidException("Ruta nije pronađena: " + key);
        }

        // dobavlja instancu kontrolera kroz DI
        Object controller = diEngine.getController(route.getControllerClass());

        // poziva metodu kontrolera
        return (Response) route.getControllerMethod().invoke(controller, request);
    }
}