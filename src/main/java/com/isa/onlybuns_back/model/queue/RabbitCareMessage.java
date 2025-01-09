package com.isa.onlybuns_back.model.queue;

import com.isa.onlybuns_back.model.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RabbitCareMessage {
    private String name;
    private Address address;
}
