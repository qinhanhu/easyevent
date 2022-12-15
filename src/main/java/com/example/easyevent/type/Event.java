package com.example.easyevent.type;

import com.example.easyevent.entity.EventEntity;
import com.example.easyevent.util.DateUtil;
import lombok.Data;

@Data
public class Event {
    private String id;
    private String title;
    private String description;
    private Float price;
    private String date;
    private Integer creatorId;
    private User creator;

    public static Event fromEntity(EventEntity eventEntity) {
        Event event = new Event();
        event.setId(eventEntity.getId().toString());
        event.setTitle(eventEntity.getTitle());
        event.setDescription(eventEntity.getDescription());
        event.setPrice(eventEntity.getPrice());
        event.setDate(DateUtil.formatDateInISOString(eventEntity.getDate()));
        event.setCreatorId(eventEntity.getCreatorId());
        return event;
    }
}
