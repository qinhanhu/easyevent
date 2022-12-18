package com.example.easyevent.type;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateUserResponse {
    private User user;
    private BaseResponse baseResponse;
}
