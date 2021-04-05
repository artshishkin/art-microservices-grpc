package net.shyshkin.study.grpc.grpcintro.client.rpctypes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.TransferRequest;
import net.shyshkin.study.grpc.grpcintro.models.TransferResponse;
import net.shyshkin.study.grpc.grpcintro.models.TransferServiceGrpc;
import net.shyshkin.study.grpc.grpcintro.models.TransferStatus;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.TransferService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
class TransferServiceNonBlockingClientTest {

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
        TestResultWrapper testResult = new TestResultWrapper();
        int fromAccountId = 10;
        int toAccountId = 9;
        int chunkAmount = 30;
        int expectedChunksCount = 5;
        int expectedFinalFromBalance = fromAccountId * 111 - chunkAmount * expectedChunksCount;
        int expectedFinalToBalance = toAccountId * 111 + chunkAmount * expectedChunksCount;

        //when
        StreamObserver<TransferResponse> responseObserver = new TestTransferResponseStreamObserver(testResult);
        StreamObserver<TransferRequest> transferRequestStreamObserver = nonBlockingStub.transfer(responseObserver);

        for (int i = 0; i < expectedChunksCount; i++) {
            TransferRequest request = TransferRequest.newBuilder()
                    .setFromAccount(fromAccountId)
                    .setToAccount(toAccountId)
                    .setAmount(chunkAmount)
                    .build();
            transferRequestStreamObserver.onNext(request);
        }

        transferRequestStreamObserver.onCompleted();

        //then
        countDownLatch.await();
        assertAll(
                () -> assertThat(testResult.getChunksCount()).isEqualTo(expectedChunksCount),
                () -> assertThat(testResult.getFromAccountId()).isEqualTo(fromAccountId),
                () -> assertThat(testResult.getToAccountId()).isEqualTo(toAccountId),
                () -> assertThat(testResult.getFromBalance()).isEqualTo(expectedFinalFromBalance),
                () -> assertThat(testResult.getToBalance()).isEqualTo(expectedFinalToBalance)
        );
    }

    private class TestTransferResponseStreamObserver implements StreamObserver<TransferResponse> {

        private final TestResultWrapper testResult;

        private TestTransferResponseStreamObserver(TestResultWrapper testResult) {
            this.testResult = testResult;
        }

        @Override
        public void onNext(TransferResponse transferResponse) {
            log.debug("Received {}", transferResponse);
            boolean allTransfersSuccess = testResult.isAllTransfersSuccess();
            allTransfersSuccess &= transferResponse.getStatus() == TransferStatus.SUCCESS;
            testResult.setAllTransfersSuccess(allTransfersSuccess);
            testResult.incrementChunksCount();
            testResult.setFromBalance(transferResponse.getAccountBalances(0).getAmount());
            testResult.setFromAccountId(transferResponse.getAccountBalances(0).getAccountNumber());
            testResult.setToBalance(transferResponse.getAccountBalances(1).getAmount());
            testResult.setToAccountId(transferResponse.getAccountBalances(1).getAccountNumber());
        }

        @Override
        public void onError(Throwable t) {
            log.error("Exception occurred", t);
        }

        @Override
        public void onCompleted() {
            log.debug("onCompleted");
            countDownLatch.countDown();
        }
    }

    @Getter
    @Setter
    private static class TestResultWrapper {
        int fromBalance = 0;
        int toBalance = 0;
        int chunksCount = 0;
        int fromAccountId;
        int toAccountId;
        boolean allTransfersSuccess = true;

        public void incrementChunksCount() {
            chunksCount++;
        }
    }
}