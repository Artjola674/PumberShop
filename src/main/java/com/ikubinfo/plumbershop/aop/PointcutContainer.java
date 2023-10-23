package com.ikubinfo.plumbershop.aop;

import org.aspectj.lang.annotation.Pointcut;

public class PointcutContainer {

    @Pointcut("execution(* com.ikubinfo.plumbershop.*.*.*Controller.*(..))")
    public static void beforeControllerPointcut(){}

//    @Pointcut("execution(* com.ikubinfo.plumbershop.*.*.ProductController.*(..)) " +
//            "|| execution(* com.ikubinfo.plumbershop.*.*.OrderController.*(..)) " +
//            "|| execution(* com.ikubinfo.plumbershop.*.*.UserController.*(..))")
//    public static void aroundPointcut(){}


    private PointcutContainer() {
    }
}
