package net.shyshkin.study.grpc.grpcintro.client.metadata;

import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.*;
import net.shyshkin.study.grpc.grpcintro.server.deadline.DeadlineService;
import net.shyshkin.study.grpc.grpcintro.server.metadata.AuthInterceptor;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class MetadataClientTest {

    public static final String VALID_TOKEN = "SuperSecretToken";
    public static final String INVALID_TOKEN = "InvalidToken";

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private static Server server;
    private static AccountDatabase accountDatabase;

    @BeforeAll
    static void beforeAll() throws IOException {

        accountDatabase = new AccountDatabase();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 6363)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(ClientConstants.getClientToken()))
                .usePlaintext()
                .build();

        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
    }

    private static void startServerWIthClientToken(String token) throws IOException {
        server = ServerBuilder
                .forPort(6363)
                .intercept(new AuthInterceptor(token))
                .addService(new DeadlineService(accountDatabase))
                .build();

        log.debug("Starting gRPC server");
        server.start();
    }

    @AfterEach
    void tearDown() {
        log.debug("Shutdown gRPC server");
        server.shutdown();
    }

    @Test
    @DisplayName("When tokens on client and server sides match request should be processed")
    void balanceTest_OK() throws IOException {
        //given
        startServerWIthClientToken(VALID_TOKEN);

        int accountNumber = 33;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        Balance balance = blockingStub
                .getBalance(balanceCheckRequest);

        //then
        log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        assertEquals(accountNumber * 111, balance.getAmount());
    }

    @Test
    @DisplayName("When tokens on client and server sides do not match request should be rejected")
    void balanceTest_FAIL() throws IOException {
        //given
        startServerWIthClientToken(INVALID_TOKEN);

        int accountNumber = 33;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Balance balance = blockingStub
                    .getBalance(balanceCheckRequest);
            log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
            assertEquals(accountNumber * 111, balance.getAmount());
        };

        //then
        assertThatThrownBy(exec).isInstanceOf(StatusRuntimeException.class)
                .hasMessage("UNAUTHENTICATED: invalid/expired token");
    }

    @Test
    void withdrawTest_validToken() throws IOException {
        //given
        startServerWIthClientToken(VALID_TOKEN);

        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(40)
                .build();

        //when
        Iterator<Money> moneyIterator = blockingStub
                .withdraw(withdrawRequest);

        //then
        List<Money> moneyList = new ArrayList<>();
        moneyIterator.forEachRemaining(moneyList::add);
        assertEquals(4, moneyList.size());
        moneyList.forEach(money -> assertEquals(10, money.getValue()));
    }

    @Test
    void withdrawTest_fail() throws IOException {
        //given
        startServerWIthClientToken(INVALID_TOKEN);

        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(40)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Iterator<Money> moneyIterator = blockingStub
                    .withdraw(withdrawRequest);
            List<Money> moneyList = new ArrayList<>();
            moneyIterator.forEachRemaining(moneyList::add);
            assertEquals(4, moneyList.size());
            moneyList.forEach(money -> assertEquals(10, money.getValue()));
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("UNAUTHENTICATED: invalid/expired token");
    }
}