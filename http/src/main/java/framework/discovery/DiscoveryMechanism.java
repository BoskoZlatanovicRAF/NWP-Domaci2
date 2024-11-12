package framework.discovery;

import framework.annotations.*;
import framework.di.DIEngine;
import framework.route.RouteHandler;
import framework.request.enums.Method;
import framework.request.exceptions.DependencyResolutionException;

import java.io.File;
import java.util.*;

public class DiscoveryMechanism {
    private final DIEngine diEngine;
    private final RouteHandler routeHandler;
    private final List<Class<?>> beanClasses;
    private final List<Class<?>> serviceClasses;
    private final List<Class<?>> componentClasses;
    private final List<Class<?>> qualifiedClasses;
    private final List<Class<?>> controllerClasses;

    public DiscoveryMechanism(RouteHandler routeHandler, DIEngine diEngine) {
        this.routeHandler = routeHandler;
        this.diEngine = diEngine;
        this.beanClasses = new ArrayList<>();
        this.serviceClasses = new ArrayList<>();
        this.componentClasses = new ArrayList<>();
        this.qualifiedClasses = new ArrayList<>();
        this.controllerClasses = new ArrayList<>();
    }

    /**
     * Scan packages for annotated classes
     */
    public void scan(String packageName) {
        try {
            System.out.println("\n=== Starting Component Discovery ===\n");

            // First scan all classes
            findAllClasses(packageName);

            // Then initialize dependencies through DIEngine
            System.out.println("\n=== Initializing Dependencies ===\n");
            diEngine.initializeDependencies(
                    beanClasses,
                    serviceClasses,
                    componentClasses,
                    qualifiedClasses
            );

            // Finally register routes
            System.out.println("\n=== Registering Routes ===\n");
            processControllers();

        } catch (Exception e) {
            throw new DependencyResolutionException("Error during scanning: " + e.getMessage());
        }
    }

    private void findAllClasses(String packageName) {
        try {
            String projectDir = System.getProperty("user.dir");
            String targetPath = projectDir + File.separator + "target" + File.separator + "classes" + File.separator
                    + packageName.replace('.', File.separatorChar);

            File targetDir = new File(targetPath);
            if (targetDir.exists()) {
                scanDirectory(targetDir, packageName);
            } else {
                throw new DependencyResolutionException("Target directory not found: " + targetPath);
            }
        } catch (Exception e) {
            throw new DependencyResolutionException("Error finding classes: " + e.getMessage());
        }
    }

    private void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    processClass(packageName + "." +
                            file.getName().substring(0, file.getName().length() - 6));
                }
            }
        }
    }

    private void processClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);

            // Skip interfaces, enums, and annotations
            if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
                return;
            }

            if (clazz.isAnnotationPresent(Controller.class)) {
                System.out.println("Found Controller: " + className);
                controllerClasses.add(clazz);
            }
            if (clazz.isAnnotationPresent(Service.class)) {
                System.out.println("Found Service: " + className);
                serviceClasses.add(clazz);
            }
            if (clazz.isAnnotationPresent(Component.class)) {
                System.out.println("Found Component: " + className);
                componentClasses.add(clazz);
            }
            if (clazz.isAnnotationPresent(Bean.class)) {
                System.out.println("Found Bean: " + className);
                beanClasses.add(clazz);
            }
            if (clazz.isAnnotationPresent(Qualifier.class)) {
                System.out.println("Found Qualified class: " + className);
                qualifiedClasses.add(clazz);
            }

        } catch (ClassNotFoundException e) {
            throw new DependencyResolutionException("Failed to load class: " + className);
        }
    }

    private void processControllers() {
        for (Class<?> controllerClass : controllerClasses) {
            System.out.println("Processing controller: " + controllerClass.getName());
            Method[] methods = controllerClass.getDeclaredMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(Path.class)) {
                    String path = method.getAnnotation(Path.class).value();

                    if (method.isAnnotationPresent(GET.class)) {
                        System.out.println("Registering GET route: " + path);
                        routeHandler.handleRoutes(path, framework.request.enums.Method.GET,
                                controllerClass, method);
                    }
                    else if (method.isAnnotationPresent(POST.class)) {
                        System.out.println("Registering POST route: " + path);
                        routeHandler.handleRoutes(path, framework.request.enums.Method.POST,
                                controllerClass, method);
                    }
                }
            }
        }
    }

    // Getters for discovered classes
    public List<Class<?>> getBeanClasses() {
        return Collections.unmodifiableList(beanClasses);
    }

    public List<Class<?>> getServiceClasses() {
        return Collections.unmodifiableList(serviceClasses);
    }

    public List<Class<?>> getComponentClasses() {
        return Collections.unmodifiableList(componentClasses);
    }

    public List<Class<?>> getQualifiedClasses() {
        return Collections.unmodifiableList(qualifiedClasses);
    }

    public List<Class<?>> getControllerClasses() {
        return Collections.unmodifiableList(controllerClasses);
    }
}