package net.shyshkin.study.grpc.grpcintro.server.rpctypes;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        AccountDatabase accountDatabase = new AccountDatabase();

        Server server = ServerBuilder
                .forPort(6565)
                .addService(new BankService(accountDatabase))
                .addService(new TransferService(accountDatabase))
                .build();

        server.start();

        log.debug("Server started: {}", server);

        server.awaitTermination();

//        server.shutdown();
    }
}
