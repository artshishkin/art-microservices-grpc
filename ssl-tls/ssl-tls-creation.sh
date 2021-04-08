#!/bin/bash

#create a private key first for CA (des3 is encryption standard)
openssl genrsa -des3 -out ca.key.pem 2048

#create ca certificate (This certificate is used to sign the server certificate and client will use this later)
openssl req -x509 -new -nodes -key ca.key.pem -sha256 -days 365 -out ca.cert.pem

#create a private key for your server
openssl genrsa -out localhost.key 2048

#As service owner, you create a request for sending to CA
openssl req -new -key localhost.key -out localhost.csr

#CA signs your request which you need to keep it safe
openssl x509 -req -in localhost.csr -CA ca.cert.pem -CAkey ca.key.pem -CAcreateserial -out localhost.crt -days 365

#grpc expects the localhost key in PKCS8 standard
openssl pkcs8 -topk8 -nocrypt -in localhost.key -out localhost.pem

