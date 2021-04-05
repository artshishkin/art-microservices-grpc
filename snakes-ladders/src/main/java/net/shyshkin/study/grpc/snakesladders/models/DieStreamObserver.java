package net.shyshkin.study.grpc.snakesladders.models;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.snakesladders.engine.GameEngine;

@Slf4j
@RequiredArgsConstructor
class DieStreamObserver implements StreamObserver<Die> {

    private final StreamObserver<GameState> gameStateStreamObserver;
    private final GameEngine gameEngine;

    @Override
    public void onNext(Die die) {
        log.debug("New client die received: {}", die.getValue());
        GameState gameState = gameEngine.oneRound(die);
        gameStateStreamObserver.onNext(gameState);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Exception occurred", t);
    }

    @Override
    public void onCompleted() {
        log.debug("onCompleted");
        gameStateStreamObserver.onCompleted();
    }
}
