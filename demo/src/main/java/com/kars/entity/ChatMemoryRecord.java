package com.kars.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatMemoryRecord extends BaseEntity{

    private String userId;

    private String messages;

}
