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

#####  18. Proto - Auto Generate JS File - Demo

-  cd to directory with proto file (proto/ or target/classes/)
    -  `cd "C:\Users\Admin\IdeaProjects\Study\VinothSelvaraj\art-microservices-grpc\protobuf\target\classes"`
-  run protoc plugin executable
    -  `..\protoc-plugins\protoc-3.6.1-windows-x86_64.exe --js_out=./ person.proto` - single file
    -  `..\protoc-plugins\protoc-3.6.1-windows-x86_64.exe --js_out=./ *.proto` - multiple
        -  does not work for me (Windows PowerShell)
        -  works for Git Bash

#####  30. Proto - How It Works - Demo

-  JSON - 23 bytes
-  Protobuf - 7 bytes (9 bytes with wrapper)
```
message Person{
  string name = 1;
  google.protobuf.Int32Value age = 2;
  common.Address address = 3;
  repeated common.Car car = 4;
}
```
-  JSON: 23 symbols
```json
    {"name":"Art","age":38}
```
-  Protobuf:
    -  1=Art - 3 symbols - 3 bytes
    -  2=38 - int32 - 1 byte
-  [Protobuf Encoding](https://developers.google.com/protocol-buffers/docs/encoding)    
-  1 is tag, 2 is tag, 3 is tag ...
-  Can be 100,234,12,543,... whatever
-  But
    -  1-15 - 1 byte
    -  16-2047 - 2 bytes
    -  recommendations:
        -  frequently used fields - lower tag
        -  less frequent fields - larger tag

####  Section 3: gRPC - Introduction & Unary RPC

#####  48. Unary - BloomRPC - Demo

-  [Install BloomRPC](https://github.com/uw-labs/bloomrpc/releases)
-  Start BloomRPC
    -  Protos -> Import
    -  View Proto
    -  127.0.0.1:6565
    -  getBalance
    ```json
        {
          "account_number": 10
        }
    ```          
    -  Response
    ```json
        {
          "amount": 1110
        }
    ```
    -  getBalance
    ```json
        {
          "account_number": 1000
        }
    ```   
   -  Response
    ```json
        {
          "error": "2 UNKNOWN: "
        }   
    ```
 
#####  49. Unary - Node Client

-  cd node-client
-  create empty project
    -  `npm init -y`
-  install dependencies
    -  `npm install @grpc/proto-loader grpc`    
-  copy `bank-service.proto` from Java project into NodeJS project
-  create [bank-client.js](node-client\bank-client.js)
-  start [GrpcServer](grpc-intro/src/main/java/net/shyshkin/study/grpc/grpcintro/server/GrpcServer.java)
-  `node .\bank-client.js`
    -  Received balance: 444  
    -  OK))
-  stop GrpcServer
-  `node .\bank-client.js`
    -  something bad happenedError: 14 UNAVAILABLE: failed to connect to all addresses 

####  Section 4: gRPC - Server Streaming RPC
       
#####  55. gRPC - Error Codes

-  [gRPC - Error Codes](https://developers.google.com/maps-booking/reference/grpc-api-v2/status_codes)



           