package com.sparta.newsfeedteamproject.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.stream.Collectors;

// 모든 API(Controller)가 호출될 때, Request 정보(Request URL, HTTP Method)를
// @Slf4J Logback 라이브러리를  활용하여 Log로 출력
@Slf4j(topic = "RequestLogAop")
@Aspect
@Component
@RequiredArgsConstructor
public class RequestLogAop {

    // 모든 Controller의 모든 메서드 Pointcut 지정
    @Pointcut("execution(* com.sparta.newsfeedteamproject.controller.*.*(..))")
    private void controller() {
    }

    // Controller 실행 전후에 Request 정보 Log로 출력 = void
    @Around("controller()")
    public Object logRequestInfo(ProceedingJoinPoint joinPoint) throws Throwable {

        // 요청 ContextHolder에서 서블릿 객체 속성 캐스팅해 구현체로 가지고 오기
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // 요청 null check
        if (attributes == null) {
            log.warn("Request 정보가 없습니다."); // info -> warn
            return joinPoint.proceed();
        }

        // 서블릿 객체의 속성 중 HttpServletRequest 요청 정보 가지고 오기
        HttpServletRequest request = attributes.getRequest();

        // Request URL log
        String uri = request.getRequestURL().toString();

        // Request Method log
        String method = request.getMethod().toString();

        // Request parameter log
        String params = request.getParameterMap().entrySet().stream()
                .map(entry -> entry.getKey() + " : " + String.join(",", entry.getValue()))
                .collect(Collectors.joining(" | "));

        // Controller Method Name log
        String controllerMethodName = joinPoint.getSignature().getName();


        log.info("Request HTTP Method : {}", method);
        log.info("Request URI : {}", uri);
        log.info("Request Parameter : {}", params);
        log.info("Controller Method : {}", controllerMethodName);

        return joinPoint.proceed();
    }
}
