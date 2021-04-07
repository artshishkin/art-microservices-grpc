package net.shyshkin.study.grpc.grpcintro.client.metadata;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.*;
import net.shyshkin.study.grpc.grpcintro.server.metadata.AuthInterceptorDB;
import net.shyshkin.study.grpc.grpcintro.server.metadata.MetadataSecureService;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class SessionTokenClientTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private static Server server;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        server = ServerBuilder
                .forPort(6363)
                .intercept(new AuthInterceptorDB(accountDatabase))
                .addService(new MetadataSecureService(accountDatabase))
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
    @DisplayName("Valid token request should be processed for PRIME user role fully")
    void balanceTest_OK_role_prime() throws IOException {
        //given
        int accountNumber = 33;
        String userToken = "user-token-33:prime";
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        Balance balance = blockingStub
                .withCallCredentials(new UserSessionToken(userToken))
                .getBalance(balanceCheckRequest);

        //then
        log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        assertEquals(accountNumber * 111, balance.getAmount());

    }

    @Test
    @DisplayName("Valid token request should be processed for STANDARD user role decreased by 15")
    void balanceTest_OK_role_standard() throws IOException {
        //given
        int accountNumber = 33;
        String userToken = "user-token-33:standard";
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        Balance balance = blockingStub
                .withCallCredentials(new UserSessionToken(userToken))
                .getBalance(balanceCheckRequest);

        //then
        log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        assertEquals(accountNumber * 111 - 15, balance.getAmount());

    }

    @Test
    @DisplayName("Request without JWT token should be rejected")
    void balanceTest_withoutToken() {
        //given
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

    @ParameterizedTest
    @DisplayName("Request with INVALID token should be rejected")
    @ValueSource(strings = {
            "user-token-33:absent-role",
            "wrong format",
            "wrong-format:standard",
            "user-token-k:standard",
            "user-token-321:standard", //absent
            "user-token- 1:standard"
    })
    void balanceTest_invalidToken(String token) throws IOException {
        //given
        int accountNumber = 33;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Balance balance = blockingStub
                    .withCallCredentials(new UserSessionToken(token))
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
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(40)
                .build();

        //when
        Iterator<Money> moneyIterator = blockingStub
                .withCallCredentials(new UserSessionToken("user-token-10:standard"))
                .withdraw(withdrawRequest);

        //then
        List<Money> moneyList = new ArrayList<>();
        moneyIterator.forEachRemaining(moneyList::add);
        assertEquals(4, moneyList.size());
        moneyList.forEach(money -> assertEquals(10, money.getValue()));
    }

    @Test
    void withdrawTest_withoutToken() throws IOException {
        //given
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

    @Test
    void withdrawTest_invalidToken() throws IOException {

        //given
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(40)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Iterator<Money> moneyIterator = blockingStub
                    .withCallCredentials(new UserSessionToken("user-token-444:prime"))
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