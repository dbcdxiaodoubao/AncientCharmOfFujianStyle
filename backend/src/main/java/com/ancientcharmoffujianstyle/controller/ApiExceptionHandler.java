package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<ApiResponse<Void>> bindingError(org.springframework.validation.BindException exception) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(400,
                exception.getBindingResult().getAllErrors().get(0).getDefaultMessage(), null));
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ApiResponse<Void> duplicate() {
        return ApiResponse.error("记录已存在，请勿重复提交", null);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> businessError(IllegalArgumentException exception) {
        return ApiResponse.error(exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validationError(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, message, null));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> statusError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ApiResponse<>(exception.getStatus().value(), exception.getReason(), null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> uploadTooLarge() {
        return ResponseEntity.status(413).body(new ApiResponse<>(413, "图片不能超过5MB", null));
    }
}
