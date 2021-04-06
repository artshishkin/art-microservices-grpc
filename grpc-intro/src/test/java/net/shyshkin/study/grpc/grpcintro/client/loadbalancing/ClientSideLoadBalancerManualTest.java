package net.shyshkin.study.grpc.grpcintro.client.loadbalancing;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside.AppNameResolverProvider;
import net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside.ServiceRegistry;
import net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside.SimpleMapServiceRegistry;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.BalanceCheckRequest;
import net.shyshkin.study.grpc.grpcintro.models.BankServiceGrpc;
import net.shyshkin.study.grpc.grpcintro.models.DepositRequest;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Client Side LB - Manually start GrpcServer1, GrpcServer2 then run test")
//@Disabled("Only for manual testing")
public class ClientSideLoadBalancerManualTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private static BankServiceGrpc.BankServiceStub nonBlockingStub;

    @BeforeAll
    static void beforeAll() {

        ServiceRegistry serviceRegistry = new SimpleMapServiceRegistry();
        serviceRegistry.registerInstance("bank-service", "localhost:6363");
        serviceRegistry.registerInstance("bank-service", "localhost:6364");
        AppNameResolverProvider resolverProvider = new AppNameResolverProvider(serviceRegistry);

        NameResolverRegistry.getDefaultRegistry().register(resolverProvider);

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forTarget("bank-service")
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();

        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        nonBlockingStub = BankServiceGrpc.newStub(managedChannel);
        log.debug("Initialized Stubs");
    }

    @Test
    @DisplayName("Every gRPC requests sends to different server due to LoadBalancing")
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

    @Nested
    class DepositTest {

        private CountDownLatch countDownLatch;

        @BeforeEach
        void setUp() {
            countDownLatch = new CountDownLatch(1);
        }

        @AfterEach
        void tearDown() throws InterruptedException {
            countDownLatch.await();
        }

        @Test
        @DisplayName("We have only ONE gRPC request so every chunk goes to ONE server despite LoadBalancing")
        void depositTest() throws InterruptedException {
            //given
            int accountNumber = 9;
            int depositCount = 4;
            int chunkAmount = 10;
            int expectedBalance = accountNumber * 111 + depositCount * chunkAmount;

            TestResultWrapper testResultWrapper = new TestResultWrapper();

            StreamObserver<Balance> observer = new TestDepositBalanceStreamObserver(accountNumber, testResultWrapper, countDownLatch);

            //when
            StreamObserver<DepositRequest> depositClient = nonBlockingStub.deposit(observer);

            for (int i = 0; i < depositCount; i++) {
                DepositRequest depositRequest = DepositRequest.newBuilder()
                        .setAccountNumber(accountNumber)
                        .setAmount(chunkAmount)
                        .build();
                depositClient.onNext(depositRequest);
            }
            depositClient.onCompleted();
            countDownLatch.await();
            assertThat(testResultWrapper.getBalance().getAmount()).isEqualTo(expectedBalance);
        }
    }
}
