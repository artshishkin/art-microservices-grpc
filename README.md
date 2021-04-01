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
    


