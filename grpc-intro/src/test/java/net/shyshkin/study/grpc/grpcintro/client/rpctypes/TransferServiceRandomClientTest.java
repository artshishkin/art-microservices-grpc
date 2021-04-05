package net.shyshkin.study.grpc.grpcintro.client.rpctypes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.TransferRequest;
import net.shyshkin.study.grpc.grpcintro.models.TransferResponse;
import net.shyshkin.study.grpc.grpcintro.models.TransferServiceGrpc;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.TransferService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class TransferServiceRandomClientTest {

    private static TransferServiceGrpc.TransferServiceStub nonBlockingStub;
    private static Server server;
    private CountDownLatch countDownLatch;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        server = ServerBuilder
                .forPort(6363)
                .addService(new TransferService(accountDatabase))
                .build();

        log.debug("Starting gRPC server");

        server.start();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();
        nonBlockingStub = TransferServiceGrpc.newStub(managedChannel);
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
    void transfer() throws InterruptedException {
        //given
        int expectedChunksCount = 100;

        //when
        StreamObserver<TransferResponse> responseObserver = new TransferResponseStreamObserver(countDownLatch);
        StreamObserver<TransferRequest> transferRequestStreamObserver = nonBlockingStub.transfer(responseObserver);

        for (int i = 0; i < expectedChunksCount; i++) {
            TransferRequest request = TransferRequest.newBuilder()
                    .setFromAccount(ThreadLocalRandom.current().nextInt(1, 11))
                    .setToAccount(ThreadLocalRandom.current().nextInt(1, 11))
                    .setAmount(ThreadLocalRandom.current().nextInt(1, 21))
                    .build();
            transferRequestStreamObserver.onNext(request);
        }
        transferRequestStreamObserver.onCompleted();

        //then
        countDownLatch.await();
    }
}