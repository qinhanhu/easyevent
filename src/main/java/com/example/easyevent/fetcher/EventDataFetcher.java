package com.example.easyevent.fetcher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.easyevent.custom.AuthContext;
import com.example.easyevent.entity.EventEntity;
import com.example.easyevent.entity.UserEntity;
import com.example.easyevent.fetcher.dataloader.CreatorsDataLoader;
import com.example.easyevent.mapper.EventEntityMapper;
import com.example.easyevent.mapper.UserEntityMapper;
import com.example.easyevent.type.*;
import com.netflix.graphql.dgs.*;
import com.netflix.graphql.dgs.context.DgsContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class EventDataFetcher {

    private final EventEntityMapper eventEntityMapper;
    private final UserEntityMapper userEntityMapper;

    @DgsQuery
    public List<Event> events(@InputArgument FilterEventInput filterEventInput) {
        LambdaQueryWrapper<EventEntity> lambda = Wrappers.lambdaQuery();
        lambda.like(filterEventInput.getTitle() != null, EventEntity::getTitle, filterEventInput.getTitle())
                .like(filterEventInput.getDescription() != null, EventEntity::getDescription, filterEventInput.getDescription())
                .ge(filterEventInput.getMinPrice() != null, EventEntity::getPrice, filterEventInput.getMinPrice())
                .le(filterEventInput.getMaxPrice() != null, EventEntity::getPrice, filterEventInput.getMaxPrice())
                .ge(filterEventInput.getDataFrom() != null, EventEntity::getDate,filterEventInput.getDataFrom())
                .le(filterEventInput.getDateTo() != null, EventEntity::getDate,filterEventInput.getDateTo());


        List<EventEntity> eventEntityList = eventEntityMapper.selectList(lambda);
        List<Event> eventList = eventEntityList.stream()
                .map(Event::fromEntity).collect(Collectors.toList());

        return eventList;
    }


    @DgsMutation
    public Event createEvent(@InputArgument(name = "eventInput") EventInput input, DataFetchingEnvironment dfe) {
        AuthContext authContext = DgsContext.getCustomContext(dfe);
        authContext.ensureAuthenticated();

        EventEntity newEventEntity = EventEntity.fromEventInput(input);
        newEventEntity.setCreatorId(authContext.getUserEntity().getId());

        eventEntityMapper.insert(newEventEntity);

        Event newEvent = Event.fromEntity(newEventEntity);

        return newEvent;
    }

    @DgsMutation
    public BaseResponse deleteEvent(@InputArgument String eventId) {
        eventEntityMapper.deleteById(Integer.parseInt(eventId));
        return new BaseResponse().setCode(200).setMsg("Success");
    }

    @DgsMutation
    public UpdateEventResponse updateEvent(@InputArgument UpdateEventInput updateEventInput) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(updateEventInput.getId());
        eventEntity.setTitle(updateEventInput.getTitle());
        eventEntity.setPrice(updateEventInput.getPrice());
        eventEntity.setDate(updateEventInput.getDate());
        eventEntity.setDescription(updateEventInput.getDescription());
        eventEntityMapper.updateById(eventEntity);
        EventEntity theEventEntity = eventEntityMapper.selectById(updateEventInput.getId());
        return new UpdateEventResponse()
                .setEvent(Event.fromEntity(theEventEntity))
                .setBaseResponse(new BaseResponse().setCode(200).setMsg("Sucess"));
    }

//    @DgsData(parentType = "Event", field = "creator")
//    public User creator(DgsDataFetchingEnvironment dfe) {
//        Event event = dfe.getSource();
//        UserEntity userEntity = userEntityMapper.selectById(event.getCreatorId());
//        User user = User.fromEntity(userEntity);
//        return user;
//    }

    @DgsData(parentType = "Event", field = "creator")
    public CompletableFuture<User> creator(DgsDataFetchingEnvironment dfe) {
        Event event = dfe.getSource();
        log.info("Fetching creator wit id: {}", event.getCreatorId());
        DataLoader<Integer, User> dataLoader = dfe.getDataLoader(CreatorsDataLoader.class);

        return dataLoader.load(event.getCreatorId());
    }
}
