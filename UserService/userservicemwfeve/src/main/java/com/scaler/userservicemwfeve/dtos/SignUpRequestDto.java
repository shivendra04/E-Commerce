package com.scaler.userservicemwfeve.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignUpRequestDto {
    private String email;
    private String password;
    private String name;
    /** Role names (e.g. USER, ADMIN). If null or empty, USER is assigned. */
    private List<String> roles;
}
