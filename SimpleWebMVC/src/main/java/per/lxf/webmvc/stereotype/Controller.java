package per.lxf.webmvc.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 作用范围
@Target(ElementType.TYPE)
// 作用时机
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Controller {
    String value() default "";
}
