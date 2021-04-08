package net.shyshkin.study.grpc.grpcintro.client.metadata;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.*;
import net.shyshkin.study.grpc.grpcintro.server.metadata.AuthInterceptorDB;
import net.shyshkin.study.grpc.grpcintro.server.metadata.MetadataSecureService;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Nested
    class UnaryRequestTests {

        @ParameterizedTest
        @DisplayName("PRIME user with VALID token has access to all accounts' balances")
        @ValueSource(ints = {33, 3, 7})
        void balanceTest_OK_role_prime(int accountNumber) {
            //given

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
        @DisplayName("STANDARD user with VALID token has access to his balance")
        void balanceTest_OK_role_standard() {

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
            assertEquals(accountNumber * 111, balance.getAmount());

        }

        @Test
        @DisplayName("STANDARD user with VALID token has access to his balance")
        void balanceTest_NO_PERMISSION_role_standard() {

            //given
            int accountNumber = 66;
            String userToken = "user-token-33:standard";
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .build();

            //when
            ThrowableAssert.ThrowingCallable exec = () -> {
                Balance balance = blockingStub
                        .withCallCredentials(new UserSessionToken(userToken))
                        .getBalance(balanceCheckRequest);
                log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
                assertEquals(accountNumber * 111, balance.getAmount());
            };

            //then
            assertThatThrownBy(exec)
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessage("PERMISSION_DENIED: You have no permissions to process operation");
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
        void balanceTest_invalidToken(String token) {
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
    }

    @Nested
    class ServerStreamingTest {

        @Test
        void withdrawTest_validToken() {

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
        void withdrawTest_withoutToken() {
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


        @ParameterizedTest
        @CsvSource(value = {
                "66,ONLY_TEN_MULTIPLE",
                "66000,INSUFFICIENT_BALANCE"
        })
        void withdrawTest_wrongAmountToWithdraw_withLog(int amountToWithdraw, ErrorMessage errorMessage) {

            //given
            int accountId = 77;
            String token = String.format("user-token-%d:standard", accountId);

            WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                    .setAccountNumber(accountId)
                    .setAmount(amountToWithdraw)
                    .build();

            //when
            try {
                Iterator<Money> moneyIterator = blockingStub
                        .withCallCredentials(new UserSessionToken(token))
                        .withdraw(withdrawRequest);

                //then
                List<Money> moneyList = new ArrayList<>();
                moneyIterator.forEachRemaining(moneyList::add);
                log.debug("{}", moneyList.size());
            } catch (StatusRuntimeException e) {
                Status status = e.getStatus();
                Metadata metadata = e.getTrailers();
                log.debug("Status is {}", status);
                assertThat(status).isEqualTo(Status.FAILED_PRECONDITION);
                log.debug("Metadata: {}", metadata);
                WithdrawalError withdrawalError = metadata.get(ClientConstants.WITHDRAWAL_ERROR_KEY);
                log.debug("WithdrawalError.amount: {}", withdrawalError.getAmount());
                assertThat(withdrawalError.getAmount()).isEqualTo(111 * accountId);
                log.debug("WithdrawalError.errorMessage: {}", withdrawalError.getErrorMessage());
                assertThat(withdrawalError.getErrorMessage()).isEqualTo(errorMessage);
            }
        }

        @ParameterizedTest
        @CsvSource(value = {
                "66,ONLY_TEN_MULTIPLE",
                "66000,INSUFFICIENT_BALANCE"
        })
        void withdrawTest_wrongAmountToWithdraw_withLog_fromThrowable(int amountToWithdraw, ErrorMessage errorMessage) {

            //given
            int accountId = 77;
            String token = String.format("user-token-%d:standard", accountId);

            WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                    .setAccountNumber(accountId)
                    .setAmount(amountToWithdraw)
                    .build();

            //when
            try {
                Iterator<Money> moneyIterator = blockingStub
                        .withCallCredentials(new UserSessionToken(token))
                        .withdraw(withdrawRequest);

                //then
                List<Money> moneyList = new ArrayList<>();
                moneyIterator.forEachRemaining(moneyList::add);
                log.debug("{}", moneyList.size());
            } catch (Exception e) {
                Status status = Status.fromThrowable(e);
                Metadata metadata = Status.trailersFromThrowable(e);
                log.debug("Status is {}", status);
                assertThat(status).isEqualTo(Status.FAILED_PRECONDITION);
                log.debug("Metadata: {}", metadata);
                WithdrawalError withdrawalError = metadata.get(ClientConstants.WITHDRAWAL_ERROR_KEY);
                log.debug("WithdrawalError.amount: {}", withdrawalError.getAmount());
                assertThat(withdrawalError.getAmount()).isEqualTo(111 * accountId);
                log.debug("WithdrawalError.errorMessage: {}", withdrawalError.getErrorMessage());
                assertThat(withdrawalError.getErrorMessage()).isEqualTo(errorMessage);
            }
        }

        @ParameterizedTest
        @CsvSource(value = {
                "66,ONLY_TEN_MULTIPLE",
                "66000,INSUFFICIENT_BALANCE"
        })
        void withdrawTest_wrongAmountToWithdraw(int amountToWithdraw, ErrorMessage errorMessage) {

            //given
            int accountId = 81;
            String token = String.format("user-token-%d:standard", accountId);

            WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                    .setAccountNumber(accountId)
                    .setAmount(amountToWithdraw)
                    .build();

            //when
            ThrowableAssert.ThrowingCallable exec = () -> {
                Iterator<Money> moneyIterator = blockingStub
                        .withCallCredentials(new UserSessionToken(token))
                        .withdraw(withdrawRequest);

                List<Money> moneyList = new ArrayList<>();
                moneyIterator.forEachRemaining(moneyList::add);
                log.debug("{}", moneyList.size());
            };

            //then
            assertThatThrownBy(exec)
                    .isInstanceOf(StatusRuntimeException.class)
                    .satisfies(exc ->
                            assertThat(Status.fromThrowable(exc))
                                    .isEqualTo(Status.FAILED_PRECONDITION))
                    .satisfies(exc ->
                            assertThat(Status.trailersFromThrowable(exc).get(ClientConstants.WITHDRAWAL_ERROR_KEY))
                                    .hasFieldOrPropertyWithValue("amount", accountId * 111)
                                    .hasFieldOrPropertyWithValue("errorMessage", errorMessage));
        }
    }


    @Nested
    class OneOfErrorHandlingTests {

        @ParameterizedTest
        @DisplayName("PRIME user with VALID token has access to all accounts' balances")
        @ValueSource(ints = {33, 3, 7})
        void balanceTest_OK_role_prime(int accountNumber) {
            //given

            String userToken = "user-token-33:prime";
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .build();

            //when
            BalanceResponse balanceOrError = blockingStub
                    .withCallCredentials(new UserSessionToken(userToken))
                    .getBalanceOrError(balanceCheckRequest);

            //then
            assertThat(balanceOrError.hasBalance()).isTrue();
            assertThat(balanceOrError.hasError()).isFalse();
            Balance balance = balanceOrError.getBalance();
            log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
            assertEquals(accountNumber * 111, balance.getAmount());
        }

        @Test
        @DisplayName("STANDARD user with VALID token has access to his balance")
        void balanceTest_OK_role_standard() {

            //given
            int accountNumber = 33;
            String userToken = "user-token-33:standard";
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .build();

            //when
            BalanceResponse balanceOrError = blockingStub
                    .withCallCredentials(new UserSessionToken(userToken))
                    .getBalanceOrError(balanceCheckRequest);

            //then
            assertThat(balanceOrError.hasBalance()).isTrue();
            assertThat(balanceOrError.hasError()).isFalse();
            Balance balance = balanceOrError.getBalance();
            log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
            assertEquals(accountNumber * 111, balance.getAmount());
        }

        @Test
        @DisplayName("STANDARD user with VALID token has NO access to another balance")
        void balanceTest_NO_PERMISSION_role_standard() {

            //given
            int accountNumber = 66;
            String userToken = "user-token-33:standard";
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .build();

            //when
            BalanceResponse balanceOrError = blockingStub
                    .withCallCredentials(new UserSessionToken(userToken))
                    .getBalanceOrError(balanceCheckRequest);

            //then
            assertThat(balanceOrError.hasBalance()).isFalse();
            assertThat(balanceOrError.hasError()).isTrue();
            BalanceRequestError error = balanceOrError.getError();
            assertThat(error.getErrorMessage()).isEqualTo(ErrorMessage.PERMISSION_DENIED);
        }

        @Test
        @DisplayName("STANDARD BLOCKED user with VALID token has NO access to his balance")
        void balanceTest_BLOCKED_ACCOUNT_role_standard() {

            //given
            int accountNumber = MetadataSecureService.FAKE_BLOCKED_ACCOUNT_ID;
            String userToken = String.format("user-token-%d:standard", accountNumber);
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .build();

            //when
            BalanceResponse balanceOrError = blockingStub
                    .withCallCredentials(new UserSessionToken(userToken))
                    .getBalanceOrError(balanceCheckRequest);

            //then
            assertThat(balanceOrError.hasBalance()).isFalse();
            assertThat(balanceOrError.hasError()).isTrue();
            BalanceRequestError error = balanceOrError.getError();
            assertThat(error.getErrorMessage()).isEqualTo(ErrorMessage.ACCOUNT_BLOCKED);
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
                BalanceResponse balanceOrError = blockingStub
                        .getBalanceOrError(balanceCheckRequest);
                Balance balance = balanceOrError.getBalance();
                log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
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
        void balanceTest_invalidToken(String token) {
            //given
            int accountNumber = 33;
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .build();

            //when
            ThrowableAssert.ThrowingCallable exec = () -> {
                BalanceResponse balanceOrError = blockingStub
                        .withCallCredentials(new UserSessionToken(token))
                        .getBalanceOrError(balanceCheckRequest);
                Balance balance = balanceOrError.getBalance();
                log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
                assertEquals(accountNumber * 111, balance.getAmount());
            };

            //then
            assertThatThrownBy(exec).isInstanceOf(StatusRuntimeException.class)
                    .hasMessage("UNAUTHENTICATED: invalid/expired token");
        }
    }
}