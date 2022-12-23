# EasyEvent Demo
## A GraphQL + SpringBoot + PostgreSQL + React App

## Features

- User
    - Create user
    - Log in
    - Delete user
    - Book events
    - Cancle bookings
    - Search events
- Event
    - Creat Event
    - Delete Event
    - Update Event Infomation

## Getting started

EasyEvent requires [Maven](https://maven.apache.org/what-is-maven.html) and [Node.js](https://nodejs.org/en/) to run.<br>
On MacOS, you can install them using brew.
```shell
brew install maven
brew install node
```

First, run Spring Boot service. - Backend
```sh
cd easyevent
mvn spring-boot:run
```
Verify the deployment by navigating to your server address in http://localhost:8080/graphiql.

Then, run React service. - Frontend

```sh
cd easyevent/frontend
npm install
npm start
```


Now you are able to open http://localhost:3000/auth and see the UI.

This Demo used AWS RDS PostgreSQL as a cloud database, you don't need to install DB in your local enviroment.


## Tech

EasyEvent uses a number of open source projects to work properly:

- [GraphQL]
- [Spring Boot]
- [PostgreSQL]
- [React]
- [Netflix DGS]
- [Mybatis-Plus]

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

   [GraphQL]: <https://graphql.org/>
   [Spring Boot]: <https://spring.io/>
   [PostgreSQL]: <https://www.postgresql.org/>
   [React]: <https://reactjs.org/>
   [Netflix DGS]: <https://netflix.github.io/dgs/>
   [Mybatis-Plus]: <https://github.com/baomidou/mybatis-plus>
