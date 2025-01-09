package com.isa.onlybuns_back.model.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdMessage {
    private String description;
    private String username;
    private Date postTime;
}
