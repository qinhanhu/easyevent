package com.example.easyevent.type;

import lombok.Data;

import java.util.Date;

@Data
public class FilterEventInput {
    private String title;
    private String description;
    private Float minPrice;
    private Float maxPrice;
    private Date dataFrom;
    private Date dateTo;
}
