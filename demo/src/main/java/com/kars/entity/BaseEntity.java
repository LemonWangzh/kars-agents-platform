package com.kars.entity;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {

    private Long id;

    private String createUser;

    private LocalDateTime createDateTime;

    private String updateUser;

    private LocalDateTime UpdateDateTime;

    private Boolean deleted;

}
