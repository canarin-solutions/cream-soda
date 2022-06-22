package solutions.canarin.cream.soda.core;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Collections.singleton;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class CreamSoda implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(CreamSoda.class);

    private static final Queue<Object> CONFIGURATIONS = new ConcurrentLinkedQueue<>();
    private static final Map<Class<?>, Class<?>> COMPONENTS = new ConcurrentHashMap<>();

    private final Map<Key<?>, Provider<?>> providers = new ConcurrentHashMap<>();
    private final Map<Key<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Set<Key<?>> autos = new HashSet<>();

    public static void addConfig(Object... configurations) {
        CONFIGURATIONS.addAll(Arrays.asList(configurations));
    }

    public static void addConfig(Iterable<Object> configurations) {
        for (Object config : configurations) {
            CONFIGURATIONS.add(config);
        }
    }

    /**
     * Adds the given components classes to the injectable ones
     *
     * @param components
     */
    public static void add(Class<?>... components) {
        Arrays.stream(components).forEach(c -> COMPONENTS.put(c, c));
    }

    /**
     * @return the context started with the given additional configurations
     */
    public static CreamSoda start(Object... configurations) {
        CreamSoda.addConfig(configurations);
        return CreamSoda.start();
    }

    /**
     * @return the context started with the given additional configurations
     */
    public static CreamSoda start(Iterable<Object> configurations) {
        CreamSoda.addConfig(configurations);
        return CreamSoda.start();
    }

    /**
     * @return the context started with the previously provided configuration
     */
    public static CreamSoda start() {
        LOG.debug("Starting injector with {} configuration modules", CONFIGURATIONS.size());
        return new CreamSoda();
    }

    @Override
    public void close() {
        LOG.debug("Closing injector");
        this.providers.clear();
        this.singletons.clear();
    }

    private CreamSoda() {
        providers.put(Key.of(CreamSoda.class), () -> this);
        try {
            for (final Object config : CONFIGURATIONS) {
                if (config instanceof Class) {
                    throw new CreamSodaException(String.format("%s provided as class instead of an instance.",
                            ((Class<?>) config).getName()));
                }
                for (Method providerMethod : providers(config.getClass())) {
                    providerMethod(config, providerMethod);
                }
                ofNullable(config.getClass().getAnnotation(Component.class)).map(c -> c.value())
                        .filter(Objects::nonNull).ifPresent(CreamSoda::add);
            }
            LOG.debug("Parsing {} components", COMPONENTS.size());
            COMPONENTS.keySet().forEach(this::provider);
            LOG.trace("Parsed components classes");
        } finally {
            CONFIGURATIONS.clear();
            COMPONENTS.clear();
        }
        LOG.debug("Autowired creating {} singletons", autos.size());
        autos.forEach(k -> providers.get(k).get());
        autos.clear();
    }

    /**
     * @return an instance of type
     */
    public <T> T instance(Class<T> type) {
        return provider(Key.of(type), null).get();
    }

    /**
     * @return instance specified by key (type and qualifier)
     */
    public <T> T instance(Key<T> key) {
        return provider(key, null).get();
    }

    /**
     * @return an instance of type
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> instancesOfType(Class<T> type) {
        return (List<T>) listProvider(Key.of(type)).get();
    }

    /**
     * @return provider of type
     */
    public <T> Provider<T> provider(Class<T> type) {
        return provider(Key.of(type), null);
    }

    /**
     * @return provider of key (type, qualifier)
     */
    public <T> Provider<T> provider(Key<T> key) {
        return provider(key, null);
    }

    @SuppressWarnings("unchecked")
    private <T> Provider<T> provider(final Key<T> key, Set<Key<?>> chain) {
        if (!providers.containsKey(key)) {
            if (nonNull(key.qualifier)) {
                throw new CreamSodaException("Unable to find provider for " + key);
            }
            final Constructor<?> constructor = constructor(key);
            final Provider<?>[] paramProviders = paramProviders(key, constructor.getParameterTypes(),
                    constructor.getGenericParameterTypes(), constructor.getParameterAnnotations(), chain);
            providers.put(key, singletonProvider(key,
                    !key.type.isAnnotationPresent(Prototype.class) || key.type.isAnnotationPresent(Autowired.class), () -> {
                        try {
                            return constructor.newInstance(params(paramProviders));
                        } catch (Exception e) {
                            throw new CreamSodaException(String.format("Can't instantiate %s", key.toString()), e);
                        }
                    }));
            if (key.type.isAnnotationPresent(Autowired.class)) {
                LOG.trace("To be autocreated {}", key);
                autos.add(key);
            }
        }
        return (Provider<T>) providers.get(key);
    }

    private void providerMethod(final Object module, final Method m) {
        final Key<?> key = Key.of(m.getReturnType(), qualifier(m.getAnnotations()));
        if (providers.containsKey(key)) {
            throw new CreamSodaException(
                    String.format("%s has multiple providers, configuration %s", key.toString(), module.getClass()));
        }
        boolean singleton = !(m.isAnnotationPresent(Prototype.class)
                || m.getReturnType().isAnnotationPresent(Prototype.class))
                || (m.isAnnotationPresent(Autowired.class) || m.getReturnType().isAnnotationPresent(Autowired.class));
        final Provider<?>[] paramProviders = paramProviders(key, m.getParameterTypes(), m.getGenericParameterTypes(),
                m.getParameterAnnotations(), Collections.singleton(key));
        providers.put(key, singletonProvider(key, singleton, () -> {
            try {
                return m.invoke(module, params(paramProviders));
            } catch (Exception e) {
                throw new CreamSodaException(String.format("Can't instantiate %s with provider", key.toString()), e);
            }
        }));
        if (m.isAnnotationPresent(Autowired.class) || m.getReturnType().isAnnotationPresent(Autowired.class)) {
            LOG.trace("To be autocreated {}", key);
            autos.add(key);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Provider<T> singletonProvider(final Key<?> key, boolean singleton, final Provider<T> provider) {
        if (singleton) {
            return () -> {
                if (!singletons.containsKey(key)) {
                    synchronized (singletons) {
                        if (!singletons.containsKey(key)) {
                            singletons.put(key, provider.get());
                        }
                    }
                }
                return (T) singletons.get(key);
            };
        }
        return provider;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Provider<List<?>> listProvider(final Key<?> key) {
        return () -> {
            List items = new ArrayList<>();
            providers.keySet().stream().filter(k -> key.type.isAssignableFrom(k.type)).map(providers::get)
                    .map(Provider::get).forEach(items::add);
            return items;
        };

    }

    private Provider<?>[] paramProviders(final Key<?> key, Class<?>[] parameterClasses, Type[] parameterTypes,
                                         Annotation[][] annotations, final Set<Key<?>> chain) {
        Provider<?>[] providerArray = new Provider<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> parameterClass = parameterClasses[i];
            Annotation qualifier = qualifier(annotations[i]);
            Optional<Class<?>> parametrizedType = Optional.empty();
            if (Provider.class.equals(parameterClass) || List.class.equals(parameterClass)) {
                Type type = ((ParameterizedType) parameterTypes[i]).getActualTypeArguments()[0];
                if (!(type instanceof Class)) {
                    throw new CreamSodaException("Unable to inject parameterized type \"" + type.toString() + "\"");
                }
                parametrizedType = Optional.of((Class<?>) type);
            }

            // we handle special cases of a Provider and unqualified List
            if (parametrizedType.isPresent() && (Provider.class.equals(parameterClass)
                    || (List.class.equals(parameterClass) && isNull(qualifier)))) {
                final Key<?> newKey = Key.of(parametrizedType.get(), qualifier);
                if (Provider.class.equals(parameterClass)) {
                    providerArray[i] = () -> provider(newKey, null);
                }
                if (List.class.equals(parameterClass)) {
                    providerArray[i] = listProvider(newKey);
                }
            } else {
                final Key<?> newKey = Key.of(parameterClass, qualifier);
                final Set<Key<?>> newChain = append(chain, key);
                if (newChain.contains(newKey)) {
                    throw new CreamSodaException(String.format("Circular dependency: %s", chain(newChain, newKey)));
                }
                providerArray[i] = () -> provider(newKey, newChain).get();
            }

        }
        return providerArray;
    }

    private static Object[] params(Provider<?>[] paramProviders) {
        Object[] params = new Object[paramProviders.length];
        for (int i = 0; i < paramProviders.length; ++i) {
            params[i] = paramProviders[i].get();
        }
        return params;
    }

    private static Set<Key<?>> append(Set<Key<?>> set, Key<?> newKey) {
        if (set != null && !set.isEmpty()) {
            Set<Key<?>> appended = new LinkedHashSet<>(set);
            appended.add(newKey);
            return appended;
        }
        return singleton(newKey);
    }

    private static String chain(Set<Key<?>> chain, Key<?> lastKey) {
        return concat(chain.stream().map(Key::toString), of(lastKey.toString())).collect(joining(" -> "));
    }

    private static Constructor<?> constructor(Key<?> key) {
        Constructor<?> inject = null;
        Constructor<?> noarg = null;
        for (Constructor<?> c : key.type.getDeclaredConstructors()) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (inject == null) {
                    inject = c;
                } else {
                    throw new CreamSodaException(String.format("%s has multiple @Inject constructors", key.type));
                }
            } else if (c.getParameterTypes().length == 0) {
                noarg = c;
            }
        }
        Constructor<?> constructor = inject != null ? inject : noarg;
        if (constructor != null) {
            constructor.setAccessible(true);
            return constructor;
        }
        throw new CreamSodaException(String.format(
                "%s doesn't have an @Inject or no-arg constructor, or a configured provider", key.type.getName()));

    }

    private static Set<Method> providers(Class<?> type) {
        Class<?> current = type;
        Set<Method> providers = new HashSet<>();
        while (!current.equals(Object.class)) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Provides.class)
                        && (type.equals(current) || !providerInSubClass(method, providers))) {
                    method.setAccessible(true);
                    providers.add(method);
                }
            }
            current = current.getSuperclass();
        }
        return providers;
    }

    private static Annotation qualifier(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                return annotation;
            }
        }
        return null;
    }

    private static boolean providerInSubClass(Method method, Set<Method> discoveredMethods) {
        for (Method discovered : discoveredMethods) {
            if (discovered.getName().equals(method.getName())
                    && Arrays.equals(method.getParameterTypes(), discovered.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }
}
