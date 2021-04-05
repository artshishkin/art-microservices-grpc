package net.shyshkin.study.grpc.snakesladders;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.snakesladders.engine.GameEngine;
import net.shyshkin.study.grpc.snakesladders.engine.GameEngineImpl;
import net.shyshkin.study.grpc.snakesladders.models.GameService;

import java.io.IOException;

@Slf4j
public class GameServer {

    public static void main(String[] args) throws InterruptedException, IOException {

        GameEngine gameEngine = new GameEngineImpl();
        GameService gameService = new GameService(gameEngine);

        Server server = ServerBuilder.forPort(6565)
                .addService(gameService)
                .build();

        server.start();
        log.debug("Server started: {}", server);

        server.awaitTermination();
    }
}
