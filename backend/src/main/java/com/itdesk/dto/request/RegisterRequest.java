package com.itdesk.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message="first name is required")
    private String firstName;
    private String lastName;
    @NotBlank(message="email is required")
    @Email(message="invalid message format")
    private String email;
    @NotBlank(message="password is required")
    @Size(min=8,message="password must be at least 8 characters")
    private String password;

    private String jobTitle;
    private String phone;
    private Long departmentId;

}
