package com.isa.queues.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AdMessage {
    private String description;
    private Date timestamp;
    private String username;
}
