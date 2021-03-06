package com.study.login.domain.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MemberDto {
    String id;
    String password;

    public MemberDto(String id, String password) {
        this.id = id;
        this.password = password;
    }
}
