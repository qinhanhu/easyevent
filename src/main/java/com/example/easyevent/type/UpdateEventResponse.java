package com.example.easyevent.type;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateEventResponse {
    private Event event;
    private BaseResponse baseResponse;
}
