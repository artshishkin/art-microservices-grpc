# art-microservices-grpc
Tutorial - Microservices with gRPC [Java + Spring Boot + Protobuf] - from Vinoth Selvaraj (Udemy)

####  Section 1: Introduction

#####  1. Problem Statement

Problems
1.  Request & Response Protocol
    -  3 message exchanges between client and server to format TCP connection
    -  then client send the request and get the response
    -  then wait for response before send another request 
2.  HTTP Headers
    -  HTTP is stateless protocol
        -  Headers are sent in every request
        -  Carries info like Cookies
        -  Plain text - relatively large in size
        -  Can not be compressed
3.  Serialization and Deserialization
4.  API Contract
5.  Client SDK

-  Stubby - RPC Framework from Google. 15 years
-  gRPC 
    -  developed in Google
    -  released in 2016
    -  adopted by Netflix, Microsoft
             




