package net.shyshkin.study.grpc.grpcintro.client.loadbalancing;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.BalanceCheckRequest;
import net.shyshkin.study.grpc.grpcintro.models.BankServiceGrpc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Manually start GrpcServer1, GrpcServer2 and docker-compose with nginx then run test")
public class NginxManualClientTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;

    @BeforeAll
    static void beforeAll() throws IOException {

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 8585)
                .usePlaintext()
                .build();

        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        log.debug("Initialized Blocking Stub");
    }

    @Test
    void balanceTest() {
        //given
        int totalRequestCount = 30;
        Map<Integer, Integer> portMap = new HashMap<>(2);

        //when
        for (int i = 0; i < totalRequestCount; i++) {
            int accountNumber = ThreadLocalRandom.current().nextInt(1, 101);
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .build();
            Balance balance = blockingStub.getBalance(balanceCheckRequest);

            int serverPort = balance.getServerPort();
            portMap.merge(serverPort, 1, Integer::sum);

            //then
            log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
            assertEquals(accountNumber * 111, balance.getAmount());
        }

        assertThat(portMap).hasSize(2);
        log.debug("Count of server requests: {}", portMap);
        assertThat(portMap.values())
                .allSatisfy(
                        grpcServerRequestCount -> assertThat(grpcServerRequestCount)
                                .isGreaterThan(totalRequestCount / 3)
                                .isLessThan(2 * totalRequestCount / 3)
                );
    }
}
