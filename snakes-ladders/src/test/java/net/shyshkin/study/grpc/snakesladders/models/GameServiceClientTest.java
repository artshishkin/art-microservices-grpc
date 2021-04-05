package net.shyshkin.study.grpc.snakesladders.models;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.snakesladders.engine.GameEngine;
import net.shyshkin.study.grpc.snakesladders.engine.GameEngineImpl;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class GameServiceClientTest {

    private static GameServiceGrpc.GameServiceStub nonBlockingStub;
    private static Server server;
    private CountDownLatch countDownLatch;

    @BeforeAll
    static void beforeAll() throws IOException {

        GameEngine gameEngine = new GameEngineImpl();
        GameService gameService = new GameService(gameEngine);

        server = ServerBuilder
                .forPort(6363)
                .addService(gameService)
                .build();

        log.debug("Starting gRPC server");

        server.start();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();
        nonBlockingStub = GameServiceGrpc.newStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        log.debug("Shutdown gRPC server");
        server.shutdown();
    }

    @BeforeEach
    void setUp() {
        countDownLatch = new CountDownLatch(1);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        countDownLatch.await();
    }

    @Test
    void gameTest() throws InterruptedException {
        //given
        TestResultWrapper testResult = new TestResultWrapper();
        TestGameStateStreamObserver gameStateResponseObserver = new TestGameStateStreamObserver(testResult, countDownLatch);
        StreamObserver<Die> clientDieStreamObserver = nonBlockingStub.roll(gameStateResponseObserver);
        gameStateResponseObserver.setDieStreamObserver(clientDieStreamObserver);

        //when
        gameStateResponseObserver.roll();

        //then
        countDownLatch.await();
        GameState finalGameState = testResult.getGameState();
        assertThat(finalGameState.getPlayerList())
                .anySatisfy(p -> assertThat(p.getPosition()).isEqualTo(TestGameStateStreamObserver.FINAL_POSITION));
    }

    @RequiredArgsConstructor
    private static class TestGameStateStreamObserver implements StreamObserver<GameState> {

        public static final int FINAL_POSITION = 100;

        private final TestResultWrapper testResult;
        private final CountDownLatch latch;

        @Setter
        private StreamObserver<Die> dieStreamObserver;

        @Override
        public void onNext(GameState gameState) {

            Player client = gameState.getPlayer(0);
            Player server = gameState.getPlayer(1);

            testResult.setGameState(gameState);

            gameState.getPlayerList()
                    .forEach(p -> log.debug("{} : {}", p.getName(), p.getPosition()));
            boolean gameOver = gameState.getPlayerList()
                    .stream()
                    .anyMatch(p -> p.getPosition() == FINAL_POSITION);

            if (gameOver) {
                log.debug("Game Over");
                String winner = client.getPosition() == FINAL_POSITION ? client.getName() : server.getName();
                log.debug("{} wins", winner);
                dieStreamObserver.onCompleted();
                return;
            }
            roll();
        }

        public void roll() {
            dieStreamObserver.onNext(newDie());
        }

        private Die newDie() {
            int dieValue = ThreadLocalRandom.current().nextInt(1, 7);
            return Die.newBuilder().setValue(dieValue).build();
        }

        @Override
        public void onError(Throwable t) {
            log.error("Exception occurred", t);
            latch.countDown();
            dieStreamObserver.onCompleted();
        }

        @Override
        public void onCompleted() {
            log.debug("onCompleted");
            latch.countDown();
//            dieStreamObserver.onCompleted();
        }
    }

    @Getter
    @Setter
    private static class TestResultWrapper {
        private GameState gameState;
    }
}