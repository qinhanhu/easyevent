package com.example.easyevent.fetcher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.easyevent.custom.AuthContext;
import com.example.easyevent.entity.BookingEntity;
import com.example.easyevent.entity.EventEntity;
import com.example.easyevent.entity.UserEntity;
import com.example.easyevent.mapper.BookingEntityMapper;
import com.example.easyevent.mapper.EventEntityMapper;
import com.example.easyevent.mapper.UserEntityMapper;
import com.example.easyevent.type.Booking;
import com.example.easyevent.type.Event;
import com.example.easyevent.type.User;
import com.netflix.graphql.dgs.*;
import com.netflix.graphql.dgs.context.DgsContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class BookingDataFetcher {
    private final BookingEntityMapper bookingEntityMapper;
    private final EventEntityMapper eventEntityMapper;
    private final UserEntityMapper userEntityMapper;

    @DgsQuery
    public List<Booking> bookings(DgsDataFetchingEnvironment dfe) {
        AuthContext authContext = DgsContext.getCustomContext(dfe);
        authContext.ensureAuthenticated();
        LambdaQueryWrapper<BookingEntity> lambda = Wrappers.lambdaQuery();
        lambda.eq(BookingEntity::getUserId, authContext.getUserEntity().getId());
        List<BookingEntity> bookingEntityList = bookingEntityMapper.selectList(lambda);
        List<Booking> bookings = bookingEntityList
                .stream()
                .map(Booking::fromEntity)
                .collect(Collectors.toList());
        return bookings;
    }

    @DgsMutation
    public Booking bookEvent(@InputArgument String eventId, DgsDataFetchingEnvironment dfe) {
        AuthContext authContext = DgsContext.getCustomContext(dfe);
        authContext.ensureAuthenticated();

        UserEntity userEntity = authContext.getUserEntity();

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setUserId(userEntity.getId());
        bookingEntity.setEventId(Integer.parseInt(eventId));
        bookingEntity.setCreatedAt(new Date());
        bookingEntity.setUpdatedAt(new Date());

        bookingEntityMapper.insert(bookingEntity);

        Booking booking = Booking.fromEntity(bookingEntity);

        return booking;
    }

    @DgsMutation
    public Event cancelBooking(@InputArgument String bookingId, DgsDataFetchingEnvironment dfe) {
        AuthContext authContext = DgsContext.getCustomContext(dfe);
        authContext.ensureAuthenticated();

        BookingEntity bookingEntity = bookingEntityMapper.selectById(Integer.parseInt(bookingId));
        if (bookingEntity == null) {
            throw new RuntimeException(String.format("Booking with id %s does not exist", bookingId));
        }

        Integer userId = bookingEntity.getUserId();
        UserEntity userEntity = authContext.getUserEntity();
        if (!userEntity.getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to cancel other people's booking!");
        }

        bookingEntityMapper.deleteById(Integer.parseInt(bookingId));

        Integer eventId = bookingEntity.getEventId();
        EventEntity eventEntity = eventEntityMapper.selectById(eventId);
        Event event = Event.fromEntity(eventEntity);
        return event;
    }

    @DgsData(parentType = "Booking", field = "user")
    public User user(DgsDataFetchingEnvironment dfe) {
        Booking booking = dfe.getSource();
        UserEntity userEntity = userEntityMapper.selectById(booking.getUserId());
        User user = User.fromEntity(userEntity);
        return user;
    }


    @DgsData(parentType = "Booking", field = "event")
    public Event event(DgsDataFetchingEnvironment dfe) {
        Booking booking = dfe.getSource();
        EventEntity eventEntity = eventEntityMapper.selectById(booking.getEventId());
        Event event = Event.fromEntity(eventEntity);
        return event;
    }


}
