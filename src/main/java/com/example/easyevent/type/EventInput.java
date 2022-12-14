package com.example.easyevent.type;

import lombok.Data;

@Data
public class EventInput {
    private String title;
    private String description;
    private Float price;
    private String date;
}