package net.shyshkin.study.grpc.grpcintro.client.loadbalancing;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.BalanceCheckRequest;
import net.shyshkin.study.grpc.grpcintro.models.BankServiceGrpc;
import net.shyshkin.study.grpc.grpcintro.server.loadbalancing.BankService;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Nginx starts in Testcontainers using Generic Container")
@Testcontainers
public class NginxGenericContainerClientTest {

    @Container
    public static GenericContainer nginx = new GenericContainer(DockerImageName.parse("nginx:1.15-alpine"))
            .withExposedPorts(8585)
            .withFileSystemBind("./../nginx/conf", "/etc/nginx/conf.d", BindMode.READ_ONLY);

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;

    private static final List<Server> servers = new ArrayList<>();

    // Also modify [default.conf](nginx/conf/default.conf)
    private final static int SERVERS_COUNT = 3;
    private final static int SERVERS_PORT_START = 6363;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        log.debug("Starting gRPC servers");

        for (int i = 0; i < SERVERS_COUNT; i++) {

            int port = SERVERS_PORT_START + i;
            Server server = ServerBuilder
                    .forPort(port)
                    .addService(new BankService(accountDatabase, port))
                    .build();

            servers.add(server);

            Server started = server.start();
            log.debug("Started server {}", started);
        }

        log.debug("All {} servers started successfully", SERVERS_COUNT);

        Integer nginxPort = nginx.getFirstMappedPort();
        String nginxHost = nginx.getHost();
        log.debug("Nginx started on {}:{}", nginxHost, nginxPort);

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress(nginxHost, nginxPort)
                .usePlaintext()
                .build();

        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        log.debug("Initialized Blocking Stub");
    }

    @AfterAll
    static void afterAll() {
        log.debug("Shutdown gRPC servers");
        servers.forEach(Server::shutdown);
    }

    @Test
    void balanceTest() {
        //given
        int requestsPerServer = 10;
        int delta = requestsPerServer * 10 / 100; // set error to 10 percent
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
}
