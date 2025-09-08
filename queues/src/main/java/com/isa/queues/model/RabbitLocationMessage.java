package com.isa.queues.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RabbitLocationMessage {
    private String name;
    private double latitude;
    private double longitude;
}
