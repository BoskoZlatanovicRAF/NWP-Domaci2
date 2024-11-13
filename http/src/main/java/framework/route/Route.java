package framework.route;

import framework.request.enums.Method;
public class Route {
    private final String path;
    private final Method method;
    private final Class<?> controllerClass;
    private final java.lang.reflect.Method controllerMethod;

    public Route(String path, Method method, Class<?> controllerClass, java.lang.reflect.Method controllerMethod) {
        this.path = path;
        this.method = method;
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
    }

    public String getPath() { return path; }
    public Method getMethod() { return method; }
    public Class<?> getControllerClass() { return controllerClass; }
    public java.lang.reflect.Method getControllerMethod() { return controllerMethod; }

    // pomocni metod za mapiranje ruta
    @Override
    public String toString() {
        return method + ":" + path;
    }
}
