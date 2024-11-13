package framework.di;

import framework.annotations.*;
import framework.request.exceptions.DependencyNotFoundException;
import framework.request.exceptions.DuplicateQualifierException;
import framework.request.exceptions.MissingQualifierException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyContainer {
    // Map for interface to implementation class mappings (with qualifiers)
    private final Map<Class<?>, Map<String, Class<?>>> interfaceToImplementation;

    // Map for storing singleton instances
    private final Map<Class<?>, Object> singletons;

    public DependencyContainer() {
        this.interfaceToImplementation = new ConcurrentHashMap<>();
        this.singletons = new ConcurrentHashMap<>();
    }


     // Register an implementation class for an interface with a qualifier
    public void registerImplementation(Class<?> interfaceClass, String qualifier, Class<?> implementationClass) {
        interfaceToImplementation.computeIfAbsent(interfaceClass, k -> new HashMap<>());

        if (interfaceToImplementation.get(interfaceClass).containsKey(qualifier)) {
            throw new DuplicateQualifierException(
                    "Multiple implementations found with qualifier '" + qualifier +
                            "' for interface " + interfaceClass.getName()
            );
        }

        interfaceToImplementation.get(interfaceClass).put(qualifier, implementationClass);
    }


    //  Get implementation class for an interface using a qualifier

    public Class<?> getImplementationClass(Class<?> interfaceClass, String qualifier) {
        Map<String, Class<?>> implementations = interfaceToImplementation.get(interfaceClass);
        if (implementations == null || !implementations.containsKey(qualifier)) {
            throw new DependencyNotFoundException(
                    "No implementation found for interface " + interfaceClass.getName() +
                            " with qualifier '" + qualifier + "'"
            );
        }
        return implementations.get(qualifier);
    }

    public void registerSingleton(Class<?> clazz, Object instance) {
        singletons.put(clazz, instance);
    }

    public Object getSingleton(Class<?> clazz) {
        return singletons.get(clazz);
    }


    public boolean hasSingleton(Class<?> clazz) {
        return singletons.containsKey(clazz);
    }

//
//    public void clear() {
//        interfaceToImplementation.clear();
//        singletons.clear();
//    }
}