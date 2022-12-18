package com.example.easyevent.fetcher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.easyevent.custom.AuthContext;
import com.example.easyevent.entity.EventEntity;
import com.example.easyevent.entity.UserEntity;
import com.example.easyevent.mapper.EventEntityMapper;
import com.example.easyevent.mapper.UserEntityMapper;
import com.example.easyevent.type.*;
import com.example.easyevent.util.TokenUtil;
import com.netflix.graphql.dgs.*;
import com.netflix.graphql.dgs.context.DgsContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class UserDataFetcher {
    private final UserEntityMapper userEntityMapper;
    private final EventEntityMapper eventEntityMapper;
    private final PasswordEncoder passwordEncoder;

    @DgsQuery
    public List<User> users() {
        List<UserEntity> userEntityList = userEntityMapper.selectList(null);
        List<User> userList = userEntityList.stream()
                .map(User::fromEntity)
                .collect(Collectors.toList());
        return userList;
    }

    @DgsMutation
    public CreateUserResponse createUser(@InputArgument UserInput userInput) {

        if (ensureUserNotExists(userInput) == 0) {
            return new CreateUserResponse()
                    .setBaseResponse(
                            new BaseResponse()
                                    .setCode(402)
                                    .setMsg("Account exists"));
        }

        UserEntity newUserEntity = new UserEntity();
        newUserEntity.setEmail(userInput.getEmail());
        newUserEntity.setPassword(passwordEncoder.encode(userInput.getPassword()));

        userEntityMapper.insert(newUserEntity);

        User newUser = User.fromEntity(newUserEntity);
        newUser.setPassword(null);

        return new CreateUserResponse()
                .setUser(newUser)
                .setBaseResponse(
                        new BaseResponse()
                                .setCode(200)
                                .setMsg("Success"));
    }

    @DgsMutation
    public BaseResponse deleteUser(@InputArgument String userId) {
        userEntityMapper.deleteById(Integer.parseInt(userId));
        return new BaseResponse().setCode(200).setMsg("Success");
    }



    @DgsQuery
    public AuthData login(@InputArgument LoginInput loginInput) {
        UserEntity userEntity = this.findUserByEmail(loginInput.getEmail());
        if (userEntity == null) {
            // throw new RuntimeException("使用该email地址的用户不存在！");
            log.info("Account " + loginInput.getEmail() + " doesn't exists！");
            return new AuthData()
                    .setBaseResponse(new BaseResponse()
                    .setCode(401)
                    .setMsg("Account doesn't exist, please sign up first"));
        }
        boolean match = passwordEncoder.matches(loginInput.getPassword(), userEntity.getPassword());
        if (!match) {
//            throw new RuntimeException("密码不正确！");
            log.info(loginInput.getEmail() + " Password doesn't match");
            return new AuthData()
                    .setBaseResponse(new BaseResponse()
                            .setCode(401)
                            .setMsg("Password doesn't match"));
        }

        String token = TokenUtil.signToken(userEntity.getId(), 1);

        return new AuthData()
                .setUserId(userEntity.getId())
                .setToken(token)
                .setTokenExpiration(1)
                .setBaseResponse(new BaseResponse()
                        .setCode(200)
                        .setMsg("Success"));
    }

    @DgsData(parentType = "User", field = "createdEvents")
    public List<Event> createdEvents(DgsDataFetchingEnvironment dfe) {
        User user = dfe.getSource();
        QueryWrapper<EventEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(EventEntity::getCreatorId, user.getId());
        List<EventEntity> eventEntityList = eventEntityMapper.selectList(queryWrapper);
        List<Event> eventList = eventEntityList.stream()
                .map(Event::fromEntity)
                .collect(Collectors.toList());
        return eventList;
    }

    private int ensureUserNotExists(UserInput userInput) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(UserEntity::getEmail, userInput.getEmail());
        if (userEntityMapper.selectCount(queryWrapper) >= 1) {
            log.info("Account exists");
            return 0;
        }
        return 1;
    }

    private UserEntity findUserByEmail(String email) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(UserEntity::getEmail, email);
        return userEntityMapper.selectOne(queryWrapper);
    }
}
