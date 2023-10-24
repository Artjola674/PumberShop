package com.ikubinfo.plumbershop.aop;

import org.aspectj.lang.annotation.Pointcut;

public class PointcutContainer {

    @Pointcut("execution(* com.ikubinfo.plumbershop.*.*.*Controller.*(..)) " +
            "&& !execution(* com.ikubinfo.plumbershop.*.*.AuthController.*(..))")
    public static void controllerPointcut(){}


    private PointcutContainer() {
    }
}
