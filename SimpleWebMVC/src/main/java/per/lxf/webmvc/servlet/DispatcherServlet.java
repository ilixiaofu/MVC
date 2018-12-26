package per.lxf.webmvc.servlet;

import per.lxf.webmvc.handler.BeanHandler;
import per.lxf.webmvc.handler.UrlMappingHandler;
import per.lxf.webmvc.stereotype.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DispatcherServlet extends HttpServlet {
    private static final String CONTROLLER_LOCATION = "contextConfigLocation";
    private static final String BEANS_LOCATION = "contextConfigLocations";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String controllerLocation = getServletConfig().getInitParameter(CONTROLLER_LOCATION);
        String beansLocation = getServletContext().getInitParameter(BEANS_LOCATION);
        scanPackge(controllerLocation);
        String[] packages = beansLocation.split(",");
        for (int i=0; i<packages.length; i++) {
            scanPackge(packages[i]);
        }
        doAutowired();
        urlMapping();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        doDispatche(req, resp);
    }

    private void doDispatche(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.replaceFirst(contextPath, "");
        UrlMappingHandler urlMappingHandler = UrlMappingHandler.getInstance();
        if (!urlMappingHandler.containsMethod(path)){
            try {
                resp.sendError(404, "request resource not found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Method method = urlMappingHandler.getMethod(path);
            Parameter[] parameters = method.getParameters();
            int parameterCount = method.getParameterCount();
            Object[] args = new Object[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                    RequestParam param = parameters[i].getAnnotation(RequestParam.class);
                    if (param.value().isEmpty()) {
                        continue;
                    }
                    String arg = req.getParameter(param.value());
                    if (String.class.equals(parameters[i].getType()) ) {
                        args[i] = arg;
                    } else if (Integer.class.equals(parameters[i].getType())) {
                        args[i] = Integer.parseInt(arg);
                    } else if (Double.class.equals(parameters[i].getType())) {
                        args[i] = Double.parseDouble(arg);
                    } else {
                        continue;
                    }
                } else {
                    if (HttpServletRequest.class.equals(parameters[i].getType())) {
                        args[i] = req;
                    } else if (HttpServletResponse.class.equals(parameters[i].getType())) {
                        args[i] = resp;
                    } else {
                        continue;
                    }
                }
            }
            String beanName = method.getDeclaringClass().getName();
            Object object = BeanHandler.getInstance().getBean(beanName);
            try {
                Object result = method.invoke(object, args);
                Class clazz = method.getReturnType();
                if (String.class.equals(clazz)) {
                    resp.getWriter().write(result.toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void scanPackge(String mpackage) {
        if (mpackage == null) {
            throw new NullPointerException();
        }
        List<String> classNames = new ArrayList<>();
        URL url = this.getClass().getClassLoader().getResource("/");
        String filePath = url.getFile() + mpackage.replace(".", "/") + "/";
        File rootFile = new File(filePath);
        String[] filePaths = rootFile.list();
        for (String path : filePaths) {
            File file = new File(filePath + path);
            if (file.isDirectory()) {
                scanPackge(mpackage + "." + path);
            } else {
                String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
                classNames.add(mpackage + "." + fileName);
            }
        }
        doInstance(classNames);
    }

    private void doInstance(List<String> classNames) {
        BeanHandler beanHandler = BeanHandler.getInstance();
        for (String className : classNames) {
            try {
                Class clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Controller controller = (Controller) clazz.getAnnotation(Controller.class);
                    String key = controller.value();
                    Object value = clazz.newInstance();
                    if (key.isEmpty()) {
                        key = clazz.getName();
                    }
                    beanHandler.putBean(key, value);
                } else if (clazz.isAnnotationPresent(Component.class)) {
                    Component component = (Component) clazz.getAnnotation(Component.class);
                    String key = component.value();
                    Object value = clazz.newInstance();
                    if (key.isEmpty()) {
                        key = clazz.getName();
                    }
                    beanHandler.putBean(key, value);
                } else {
                    continue;
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void doAutowired() {
        BeanHandler beanHandler = BeanHandler.getInstance();
        Set<Map.Entry<String, Object>> entrySet = beanHandler.getBeans().entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            Object object = entry.getValue();
            Class clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String key = autowired.value();
                    if (key.isEmpty()) {
                        if (beanHandler.containsBean(field.getType().getName())) {
                            key = field.getType().getName();
                            try {
                                field.set(object, beanHandler.getBean(key));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        } else {
                            key = field.getType().getName() + "Impl";
                            if (beanHandler.containsBean(key)) {
                                try {
                                    field.set(object, beanHandler.getBean(key));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        if (beanHandler.containsBean(key)) {
                            try {
                                field.set(object, beanHandler.getBean(key));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void urlMapping() {
        BeanHandler beanHandler = BeanHandler.getInstance();
        Set<Map.Entry<String, Object>> entrySet = beanHandler.getBeans().entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            Object instance = entry.getValue();
            Class clazz = instance.getClass();
            String classUrlPath = null;
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping classRequestMapping = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                classUrlPath = "/" + classRequestMapping.value();
            }
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                } else {
                    method.setAccessible(true);
                    RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                    String key = "/" + methodRequestMapping.value();
                    if (classUrlPath != null) {
                        key = classUrlPath + key;
                        key = key.replaceAll("/+", "/");
                    }
                    UrlMappingHandler urlMappingHandler = UrlMappingHandler.getInstance();
                    urlMappingHandler.putMethod(key, method);
                }
            }
        }
    }
}
