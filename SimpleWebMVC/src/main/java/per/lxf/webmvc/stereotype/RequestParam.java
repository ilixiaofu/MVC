package per.lxf.webmvc.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// 作用范围
@Target(ElementType.PARAMETER)
// 作用时机
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    String value() default "";
}