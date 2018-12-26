package per.lxf.webmvc.handler;

import java.util.HashMap;
import java.util.Map;

public class BeanHandler {

    private Map<String, Object> beans;

    private static BeanHandler instance;

    public BeanHandler() {
        this.beans = new HashMap<>();
    }

    public static BeanHandler getInstance() {
        if (instance == null) {
            synchronized (BeanHandler.class) {
                if (instance == null) {
                    instance = new BeanHandler();
                }
            }
        }
        return instance;
    }

    public Map<String, Object> getBeans() {
        return beans;
    }

    public void putBean(String var1, Object object) {
        this.beans.put(var1, object);
    }

    public Object getBean(String var1) {
        Object object = this.beans.get(var1);
        if (object == null) {
            throw new IllegalArgumentException("bean name not exists!");
        }
        return object;
    }

    public boolean containsBean(String var1) {
        return this.beans.containsKey(var1);
    }
}
