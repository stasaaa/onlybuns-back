package com.isa.onlybuns_back.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RabbitLocationMessage {
    private String name;
    private double latitude;
    private double longitude;
}