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
             
#####  2. HTTP/2-vs-HTTP/1.1

-  Ctrl+Shift+I
-  [HTTP1 with 200ms latency](https://http1.golang.org/gophertiles?latency=200)
    -  6 TCP connections
    -  Times from connection start:
    -  DOM loaded: 347ms
    -  DOM complete (images loaded): 11980ms
-  [HTTP2 with 200ms latency](https://http2.golang.org/gophertiles?latency=200)
    -  1 TCP connection
    -  Times from connection start:
    -  DOM loaded: 389ms
    -  DOM complete (images loaded): 1881ms
-  Comparison
    -  Binary
    -  Header Compression
    -  Flow Control (back pressure)
    -  Multiplexing
-  gRPC - Benefits
    -  HTTP2 is default
        -  binary
        -  multiplexing
        -  flow-control
    -  Non-blocking, Streaming bindings
    -  Protobuf
        -  Strict Typing
        -  DTO
        -  Service Definition
        -  Language agnostic
        -  Auto-generated bindings for multiple languages
    -  Great for mobile apps    
    
#####  3. gRPC-vs-REST - Performance Comparison

1.  Start microservices
    -  use [docker-compose.yml](Section%201%20-%20Introduction/gRPC-vs-REST/docker-compose.yml)
    -  `docker-compose up`
2.  Endpoints for testing    
    -  REST
        -  `/rest/unary/1000`
    -  gRPC    
        -  `/grpc/unary/1000`
        -  `/grpc/stream/1000`
3.  Curl
    -  `curl localhost:8080/rest/unary/1000` - 1.78s
    -  `curl localhost:8080/grpc/unary/1000` - 1.08s
    -  `curl localhost:8080/grpc/stream/1000` - 346ms    
4.  Apache Benchmark Commands
    -  `ab -n 1000 -c 100 localhost:8080/rest/unary/1000`
        -  too many Failed requests with Exception
            -  `java.lang.RuntimeException: oops at com.vinsguru.rest.service.RestSquareService.getSquareUnary(RestSquareService.java:19)` 
        -  Result:  [rest-unary-test-result.txt](Section%201%20-%20Introduction/gRPC-vs-REST/rest-unary-test-result.txt)
    -  `ab -n 1000 -c 100 localhost:8080/grpc/unary/1000`
        -  Result:  [grpc-unary-test-result.txt](Section%201%20-%20Introduction/gRPC-vs-REST/grpc-unary-test-result.txt)
    -  `ab -n 1000 -c 100 localhost:8080/grpc/stream/1000`
        -  Result:  [grpc-stream-test-result.txt](Section%201%20-%20Introduction/gRPC-vs-REST/grpc-stream-test-result.txt)

####  Section 2: Protocol Buffers

#####  7. Proto - Project Setup

Install plugin for IDEA - Protocol Buffer Editor from Jeremy Volkman

#####  8. Proto - A Simple Person Message Creation

-  `mvn compile`
-  target - protoc-plugins - protoc-3.6.1-windows-x86_64.exe
-  target - generated-sources - protobuf - java - PersonOuterClass.java
