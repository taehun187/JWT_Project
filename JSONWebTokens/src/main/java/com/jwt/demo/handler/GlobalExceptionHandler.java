package com.jwt.demo.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // 전역적으로 예외를 처리하는 클래스임을 나타냅니다.
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class) // 모든 종류의 Exception을 처리하도록 지정
    public ResponseEntity<String> handleException(Exception e) {
        // HTTP 상태 코드 500 (Internal Server Error)와 예외 메시지를 응답 본문에 포함하여 반환합니다.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
