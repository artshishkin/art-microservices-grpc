package net.shyshkin.study.grpc.grpcintro.server.deadline;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;

import java.io.IOException;

@Slf4j
public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        AccountDatabase accountDatabase = new AccountDatabase();

        Server server = ServerBuilder
                .forPort(6363)
                .addService(new DeadlineService(accountDatabase))
                .build();

        server.start();

        log.debug("Server started: {}", server);

        server.awaitTermination();

//        server.shutdown();
    }
}
