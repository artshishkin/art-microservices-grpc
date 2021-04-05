package net.shyshkin.study.grpc.snakesladders.models;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.shyshkin.study.grpc.snakesladders.engine.GameEngine;

@RequiredArgsConstructor
public class GameService extends GameServiceGrpc.GameServiceImplBase {

    private final GameEngine gameEngine;

    @Override
    public StreamObserver<Die> roll(StreamObserver<GameState> responseObserver) {

        StreamObserver<Die> dieStreamObserver = new DieStreamObserver(responseObserver, gameEngine);

        return dieStreamObserver;
    }
}
