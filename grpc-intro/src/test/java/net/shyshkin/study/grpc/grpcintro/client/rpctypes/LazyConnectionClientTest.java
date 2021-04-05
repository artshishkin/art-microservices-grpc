package net.shyshkin.study.grpc.grpcintro.client.rpctypes;

import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.BalanceCheckRequest;
import net.shyshkin.study.grpc.grpcintro.models.BankServiceGrpc;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.BankService;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class LazyConnectionClientTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private static Server server;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        server = ServerBuilder
                .forPort(6363)
                .addService(new BankService(accountDatabase))
                .build();

        log.debug("Starting gRPC server");

        server.start();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 6767)
                .usePlaintext()
                .build();

        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        log.debug("Initialized Blocking Stub");
    }

    @AfterAll
    static void afterAll() {
        log.debug("Shutdown gRPC server");
        server.shutdown();
    }

    @Test
    @DisplayName("Due to Lazy Init we got connection error only after our try to process gRPC call (send)")
    void lazyConnectionTest() {
        //given
        int accountNumber = 3;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();
        long start = System.currentTimeMillis();

        //when
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        ThrowableAssert.ThrowingCallable exec = () -> {
            Balance balance = blockingStub.getBalance(balanceCheckRequest);
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("UNAVAILABLE: io exception");

        assertThat(System.currentTimeMillis() - start).isGreaterThan(1000);
    }

}