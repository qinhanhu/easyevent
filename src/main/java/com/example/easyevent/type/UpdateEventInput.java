package com.example.easyevent.type;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateEventInput {
    private Integer id;
    private String title;
    private String description;
    private Float  price;
    private Date date;
}