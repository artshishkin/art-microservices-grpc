package net.shyshkin.study.grpc.grpcintro.server;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.BalanceCheckRequest;
import net.shyshkin.study.grpc.grpcintro.models.BankServiceGrpc;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class BankFutureClientTest {

    private static BankServiceGrpc.BankServiceFutureStub futureStub;
    private static Server server;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        server = ServerBuilder
                .forPort(6565)
                .addService(new BankService(accountDatabase))
                .build();

        System.out.println("Starting gRPC server");

        server.start();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();
        futureStub = BankServiceGrpc.newFutureStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Shutdown gRPC server");
        server.shutdown();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 5, 7})
    void balanceTest(int accountNumber) throws InterruptedException, ExecutionException {
        //given
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();
        int expectedBalance = accountNumber * 111;

        //when
        ListenableFuture<Balance> balanceFuture = futureStub.getBalance(balanceCheckRequest);
        Balance balance = balanceFuture.get();
        System.out.printf("Received balance: %d for user %d\n", balance.getAmount(), accountNumber);
        assertThat(balance.getAmount()).isEqualTo(expectedBalance);
    }
}