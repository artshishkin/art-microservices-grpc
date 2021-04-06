package net.shyshkin.study.grpc.grpcintro.client.deadline;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.*;
import net.shyshkin.study.grpc.grpcintro.server.deadline.DeadlineService;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class DeadlineClientTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private static Server server;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        server = ServerBuilder
                .forPort(6363)
                .addService(new DeadlineService(accountDatabase))
                .build();

        log.debug("Starting gRPC server");

        server.start();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 6363)
                .usePlaintext()
                .build();
        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        log.debug("Shutdown gRPC server");
        server.shutdown();
    }

    @Test
    void balanceTest_OK() {
        //given
        int accountNumber = 33;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        Balance balance = blockingStub
                .withDeadlineAfter(1, TimeUnit.SECONDS)
                .getBalance(balanceCheckRequest);

        //then
        log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        assertEquals(accountNumber * 111, balance.getAmount());
    }


    @Test
    void balanceTest_withDeadLineExceeded() {
        //given
        int accountNumber = 8;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Balance balance = blockingStub
                    .withDeadlineAfter(100, TimeUnit.MILLISECONDS)
                    .getBalance(balanceCheckRequest);
            log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("DEADLINE_EXCEEDED: deadline exceeded after");
    }

    @Test
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

    @Test
    void withdrawTest_notEnoughMoney() throws InterruptedException {
        //given
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(7)
                .setAmount(4000)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            log.debug("Trying to withdraw not available amount: {}", withdrawRequest.getAmount());
            Iterator<Money> moneyIterator = blockingStub.withdraw(withdrawRequest);
            List<Money> moneyList = new ArrayList<>();
            moneyIterator.forEachRemaining(moneyList::add);
            assertEquals(400, moneyList.size());
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("FAILED_PRECONDITION: Not enough money. You have only 777");
    }
}