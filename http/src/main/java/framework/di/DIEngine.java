package framework.di;

import framework.annotations.*;
import framework.request.exceptions.DependencyResolutionException;
import framework.request.exceptions.InvalidAutowiredTargetException;
import framework.request.exceptions.MissingQualifierException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

/**
  Handles dependency injection using reflection.
  Responsible for creating and injecting instances of classes.
 */
public class DIEngine {
    private final DependencyContainer container;
    private final Set<Class<?>> beingInstantiated;
    private final Map<Class<?>, Object> controllerInstances;

    public DIEngine(DependencyContainer container) {
        this.container = container;
        this.beingInstantiated = new HashSet<>();
        this.controllerInstances = new HashMap<>();
    }


    //  Initialize all dependencies at startup

    public void initializeDependencies(List<Class<?>> classes, List<Class<?>> serviceClasses, List<Class<?>> componentClasses, List<Class<?>> qualifiedClasses) {
        System.out.println("\n=== Initializing Dependencies ===");

        // First pass: Register qualified implementations
        System.out.println("\nRegistering Qualified implementations:");
        classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Qualifier.class))
                .forEach(this::registerQualifiedClass);

        // Second pass: Initialize services
        System.out.println("\nInitializing Services:");
        classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Service.class))
                .forEach(this::initializeSingleton);

        // Third pass: Initialize singleton beans
        System.out.println("\nInitializing Singleton Beans:");
        classes.stream()
                .filter(this::isSingletonBean)
                .forEach(this::initializeSingleton);

        // Log components (prototype scope)
        System.out.println("\nDiscovered Components (prototype scope):");
        classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .forEach(clazz -> System.out.println("Found Component: " + clazz.getSimpleName() +
                        " (will be initialized on demand)"));
    }

    private void registerQualifiedClass(Class<?> clazz) {
        try {
            Qualifier qualifier = clazz.getAnnotation(Qualifier.class);
            System.out.println("Registering " + clazz.getSimpleName() +
                    " with qualifier: " + qualifier.value());

            // Register with all implemented interfaces
            for (Class<?> iface : clazz.getInterfaces()) {
                container.registerImplementation(iface, qualifier.value(), clazz);
            }
        } catch (Exception e) {
            throw new DependencyResolutionException("Failed to register qualified class: " + clazz.getName());
        }
    }

    private void initializeSingleton(Class<?> clazz) {
        try {
            System.out.println("Creating instance of: " + clazz.getSimpleName());
            instantiate(clazz);
        } catch (Exception e) {
            throw new DependencyResolutionException("Failed to initialize " + clazz.getName());
        }
    }


   // Create or get an instance of a class with all dependencies injected

    public Object instantiate(Class<?> clazz) throws Exception {
        // Check for circular dependencies
        if (!beingInstantiated.add(clazz)) {
            throw new DependencyResolutionException("Circular dependency detected for class: " + clazz.getName());
        }

        try {
            // If it's a controller, use the controller cache
            if (clazz.isAnnotationPresent(Controller.class)) {
                return getControllerInstance(clazz);
            }

            // For other classes, check if it's a singleton and already exists
            if (isSingleton(clazz) && container.hasSingleton(clazz)) {
                return container.getSingleton(clazz);
            }

            // Create new instance
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // If singleton, register it immediately to handle circular dependencies
            if (isSingleton(clazz)) {
                container.registerSingleton(clazz, instance);
            }

            // Inject dependencies
            injectDependencies(instance);

            return instance;
        } finally {
            beingInstantiated.remove(clazz);
        }
    }

    private Object getControllerInstance(Class<?> controllerClass) throws Exception {
        return controllerInstances.computeIfAbsent(controllerClass, clazz -> {
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                injectDependencies(instance);
                System.out.println("Created and cached controller instance: " +
                        clazz.getSimpleName());
                return instance;
            } catch (Exception e) {
                throw new DependencyResolutionException("Failed to create controller: " +
                        clazz.getName());
            }
        });
    }


    //  Inject dependencies into an existing instance

    private void injectDependencies(Object instance) throws Exception {
        Class<?> clazz = instance.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                injectField(instance, field);
            }
        }
    }


    //  Inject a specific field
    private void injectField(Object instance, Field field) throws Exception {
        Autowired autowired = field.getAnnotation(Autowired.class);
        Class<?> fieldType = field.getType();

        // Verify the field type is injectable
        if (!isInjectableType(fieldType)) {
            throw new InvalidAutowiredTargetException("Field " + field.getName() +
                    " in " + instance.getClass().getName() + " is not a valid injectable type");
        }

        // Get implementation class if it's an interface
        Class<?> implementationClass = fieldType;
        if (fieldType.isInterface()) {
            Qualifier qualifier = field.getAnnotation(Qualifier.class);
            if (qualifier == null) {
                throw new MissingQualifierException("Missing @Qualifier for interface field " +
                        field.getName() + " in " + instance.getClass().getName());
            }
            implementationClass = container.getImplementationClass(fieldType, qualifier.value());
        }

        // Create or get the dependency instance
        Object dependency = instantiate(implementationClass);

        // Inject the dependency
        field.setAccessible(true);
        field.set(instance, dependency);

        // Log if verbose is enabled
        if (autowired.verbose()) {
            logInjection(instance, field, dependency);
        }
    }


    //  Check if a class should be treated as a singleton
    private boolean isSingleton(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Controller.class) ||
                clazz.isAnnotationPresent(Service.class)) {
            return true;
        }
        return isSingletonBean(clazz);
    }


    private boolean isSingletonBean(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Bean.class)) {
            return "singleton".equals(clazz.getAnnotation(Bean.class).scope());
        }
        return false;
    }

    private boolean isInjectableType(Class<?> type) {
        return type.isAnnotationPresent(Bean.class) ||
                type.isAnnotationPresent(Service.class) ||
                type.isAnnotationPresent(Component.class) ||
                type.isInterface(); // Interfaces are injectable with @Qualifier
    }


    //  Log injection details when verbose is true
    private void logInjection(Object instance, Field field, Object dependency) {
        System.out.printf(
                "Initialized %s %s in %s on %s with %d%n",
                field.getType().getName(),
                field.getName(),
                instance.getClass().getName(),
                LocalDateTime.now(),
                dependency.hashCode()
        );
    }
    public Object getController(Class<?> controllerClass) throws Exception {
        return getControllerInstance(controllerClass);
    }
}