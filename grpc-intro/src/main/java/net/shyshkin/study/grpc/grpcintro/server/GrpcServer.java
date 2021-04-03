package net.shyshkin.study.grpc.grpcintro.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder
                .forPort(6565)
                .addService(new BankService())
                .build();

        server.start();

        server.awaitTermination();

//        server.shutdown();
    }
}
