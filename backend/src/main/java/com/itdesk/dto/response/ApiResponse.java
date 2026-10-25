package com.itdesk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> error;
    private Instant timestamp;

    public static <T> ApiResponse<T> success(T data , String message)
    {
        return new ApiResponse<>(true,message,data,null,Instant.now());
    }
    public static <T> ApiResponse<T> success(T data)
    {
        return new ApiResponse<>(true,"operation successfull",data,null,Instant.now());
    }
    public static <T> ApiResponse<T> error(String message)
    {
        return new ApiResponse<>(false,message,null,null,Instant.now());

    }
    public static <T> ApiResponse<T> error(String message,List<String> errors)
    {
        return new ApiResponse<>(false,message,null,errors,Instant.now());
    }

}
