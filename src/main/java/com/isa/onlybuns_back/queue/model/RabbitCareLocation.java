package com.isa.onlybuns_back.queue.model;

import com.isa.onlybuns_back.model.Address;
import lombok.*;

@Getter
@Setter
@ToString
public class RabbitCareLocation {
    private String name;
    private String address;

    public RabbitCareLocation(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
