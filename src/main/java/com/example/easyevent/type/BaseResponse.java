package com.example.easyevent.type;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseResponse {
    private Integer code;
    private String msg;
}
