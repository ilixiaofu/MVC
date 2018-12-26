package per.lxf.webmvc.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class UrlMappingHandler {

    private Map<String, Method> methods;

    private static UrlMappingHandler instance;

    private UrlMappingHandler() {
        this.methods = new HashMap<>();
    }

    public static UrlMappingHandler getInstance() {
        if (instance == null) {
            synchronized (UrlMappingHandler.class) {
                if (instance == null) {
                    instance = new UrlMappingHandler();
                }
            }
        }
        return instance;
    }

    public void putMethod(String var1, Method method) {
        this.methods.put(var1, method);
    }

    public Method getMethod(String var1) {
        Method method = this.methods.get(var1);
        if (method == null) {
            throw new IllegalArgumentException("method name not exists!");
        }
        return method;
    }

    public boolean containsMethod(String var1) {
        return this.methods.containsKey(var1);
    }
}
