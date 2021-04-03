package net.shyshkin.study.grpc.grpcintro.server;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import net.shyshkin.study.grpc.grpcintro.models.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankClientTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
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
                .forAddress("localhost", 6565)
                .usePlaintext()
                .build();
        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Shutdown gRPC server");
        server.shutdown();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 5, 7})
    void balanceTest(int accountNumber) {
        //given
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        Balance balance = blockingStub.getBalance(balanceCheckRequest);

        //then
        System.out.printf("Received balance: %d for user %d", balance.getAmount(), accountNumber);
        assertEquals(accountNumber * 111, balance.getAmount());
    }

    @Test
    @Disabled("Too long to test in CI/CD. Enable for manual testing")
    void withdrawTest() {
        //given
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(40)
                .build();

        //when
        Iterator<Money> moneyIterator = blockingStub.withdraw(withdrawRequest);

        //then
        List<Money> moneyList = new ArrayList<>();
        moneyIterator.forEachRemaining(moneyList::add);
        assertEquals(4, moneyList.size());
        moneyList.forEach(money -> assertEquals(10, money.getValue()));
    }
}