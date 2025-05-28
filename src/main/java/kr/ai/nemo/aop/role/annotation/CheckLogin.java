package kr.ai.nemo.aop.role.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

@Target(ElementType.METHOD)
@Component
public @interface CheckLogin {

}
