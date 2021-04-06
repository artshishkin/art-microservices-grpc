package net.shyshkin.study.grpc.grpcintro.client.loadbalancing;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside.AppNameResolverProvider;
import net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside.ServiceRegistry;
import net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside.SimpleMapServiceRegistry;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.BalanceCheckRequest;
import net.shyshkin.study.grpc.grpcintro.models.BankServiceGrpc;
import net.shyshkin.study.grpc.grpcintro.models.DepositRequest;
import net.shyshkin.study.grpc.grpcintro.server.loadbalancing.BankService;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Client Side LB - Automation Test")
public class ClientSideLoadBalancerAutoTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private static BankServiceGrpc.BankServiceStub nonBlockingStub;

    private static final List<Server> servers = new ArrayList<>();

    private final static int SERVERS_COUNT = 3;
    private final static int SERVERS_PORT_START = 6363;

    private static final String SERVICE_NAME = "bank-service";
    private static AppNameResolverProvider resolverProvider;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        ServiceRegistry serviceRegistry = new SimpleMapServiceRegistry();
        resolverProvider = new AppNameResolverProvider(serviceRegistry);
        NameResolverRegistry.getDefaultRegistry().register(resolverProvider);

        log.debug("Starting gRPC servers");

        for (int i = 0; i < SERVERS_COUNT; i++) {

            int port = SERVERS_PORT_START + i;
            Server server = ServerBuilder
                    .forPort(port)
                    .addService(new BankService(accountDatabase, port))
                    .build();

            servers.add(server);

            serviceRegistry.registerInstance(SERVICE_NAME, "localhost:" + port);

            Server started = server.start();
            log.debug("Started server {}", started);
        }

        log.debug("All {} servers started successfully", SERVERS_COUNT);

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forTarget(SERVICE_NAME)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();

        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        nonBlockingStub = BankServiceGrpc.newStub(managedChannel);
        log.debug("Initialized Blocking Stub");
    }

    @AfterAll
    static void afterAll() {
        log.debug("Shutdown gRPC servers");
        servers.forEach(Server::shutdown);

        NameResolverRegistry.getDefaultRegistry().deregister(resolverProvider);
    }

    @Test
    @DisplayName("Every gRPC requests sends to different server due to LoadBalancing")
    void balanceTest() {
        //given
        int requestsPerServer = 10;
        int delta = requestsPerServer * 20 / 100; // set error to 20 percent
        int requestsPerServerMin = requestsPerServer - delta;
        int requestsPerServerMax = requestsPerServer + delta;

        int totalRequestCount = requestsPerServer * SERVERS_COUNT;
        Map<Integer, Integer> portMap = new HashMap<>(SERVERS_COUNT);

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

        assertThat(portMap).hasSize(SERVERS_COUNT);
        log.debug("Count of server requests: {}", portMap);
        assertThat(portMap.values())
                .allSatisfy(
                        grpcServerRequestCount -> assertThat(grpcServerRequestCount)
                                .isGreaterThan(requestsPerServerMin)
                                .isLessThan(requestsPerServerMax)
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

        @Test
        @DisplayName("MANY gRPC requests goes to different servers due to LoadBalancing")
        void depositTestManyGrpcRequests() throws InterruptedException {
            //given
            Set<Integer> serversPorts = new HashSet<>(SERVERS_COUNT);

            for (int serverIdx = 0; serverIdx < SERVERS_COUNT; serverIdx++) {

                if (serverIdx != 0) countDownLatch = new CountDownLatch(1);

                int accountNumber = 3;
                int depositCount = 4;
                int chunkAmount = 10;
                int expectedBalance = accountNumber * 111 + depositCount * chunkAmount * (1 + serverIdx);

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
                int serverPort = testResultWrapper.getBalance().getServerPort();
                serversPorts.add(serverPort);
            }
            log.debug("Servers are on ports: {}", serversPorts);
            assertThat(serversPorts)
                    .hasSize(SERVERS_COUNT)
                    .allSatisfy(
                            port -> assertThat(port)
                                    .isGreaterThanOrEqualTo(SERVERS_PORT_START)
                                    .isLessThan(SERVERS_PORT_START + SERVERS_COUNT)
                    );
        }
    }
}
