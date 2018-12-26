package per.lxf.webmvc.stereotype;

import java.lang.annotation.*;

// 作用范围
@Target(ElementType.FIELD)
// 作用时机
@Retention(RetentionPolicy.RUNTIME)

@Documented // javadoc
public @interface Autowired {
    String value() default "";
}
