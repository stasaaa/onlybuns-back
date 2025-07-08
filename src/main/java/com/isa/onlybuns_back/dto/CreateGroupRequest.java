package com.isa.onlybuns_back.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateGroupRequest {
    private String groupName;
    private Long adminId;
    private List<Long> memberIds;
}
