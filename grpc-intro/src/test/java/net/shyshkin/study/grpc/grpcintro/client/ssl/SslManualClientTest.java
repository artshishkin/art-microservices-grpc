package net.shyshkin.study.grpc.grpcintro.client.ssl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.BalanceCheckRequest;
import net.shyshkin.study.grpc.grpcintro.models.BankServiceGrpc;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.nio.file.Path;

import static io.grpc.Status.UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class SslManualClientTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private ManagedChannel managedChannel;

    @AfterEach
    void tearDown() {
        managedChannel.shutdown();
    }

    @Test
    void balanceTest_serverSSL_clientPlainText() {
        //given
        managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 6363)
                .usePlaintext()
                .build();
        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);

        int accountNumber = 6;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Balance balance = blockingStub.getBalance(balanceCheckRequest);
            log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("UNAVAILABLE: Network closed for unknown reason")
                .satisfies(ex -> assertThat(Status.fromThrowable(ex))
                        .hasFieldOrPropertyWithValue("code", UNAVAILABLE.getCode())
                        .hasFieldOrPropertyWithValue("description", "Network closed for unknown reason")
                )
                .satisfies(
                        ex -> assertThat(Status.trailersFromThrowable(ex))
                                .isNotNull()
                                .satisfies(metadata -> log.debug("Metadata: {}", metadata))
                                .satisfies(metadata -> log.debug("Metadata keys: {}", metadata.keys()))
                );
    }

    @Test
    void balanceTest_serverSSL_clientSSLNotConfigured() {
        //given
        managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 6363)
                .build();
        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);

        int accountNumber = 6;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Balance balance = blockingStub.getBalance(balanceCheckRequest);
            log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("UNAVAILABLE: io exception")
                .satisfies(ex -> assertThat(Status.fromThrowable(ex))
                        .hasFieldOrPropertyWithValue("code", UNAVAILABLE.getCode())
                )
                .satisfies(
                        ex -> assertThat(Status.trailersFromThrowable(ex))
                                .isNotNull()
                                .satisfies(metadata -> log.debug("Metadata: {}", metadata))
                                .satisfies(metadata -> log.debug("Metadata keys: {}", metadata.keys()))
                )
                .hasCauseInstanceOf(SSLHandshakeException.class)
                .getCause().hasMessage("General OpenSslEngine problem")
        ;
    }

    @Test
    void balanceTest_serverSSL_clientSSL_configured() throws SSLException {
        //given
        Path sslDirectory = Path.of("./../ssl-tls/").toAbsolutePath().normalize();
        File trustCertCollectionFile = sslDirectory.resolve("ca.cert.pem").toFile();

        SslContext sslContext = GrpcSslContexts.forClient()
                .trustManager(trustCertCollectionFile)
                .build();

        managedChannel = NettyChannelBuilder
                .forAddress("localhost", 6363)
                .sslContext(sslContext)
                .build();
        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);

        int accountNumber = 6;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        Balance balance = blockingStub.getBalance(balanceCheckRequest);

        //then
        log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        assertEquals(accountNumber * 111, balance.getAmount());
    }
}