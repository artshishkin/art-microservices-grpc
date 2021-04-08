package net.shyshkin.study.grpc.grpcintro.server.ssl;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        AccountDatabase accountDatabase = new AccountDatabase();

        Path sslDirectory = Path.of("./ssl-tls/").toAbsolutePath().normalize();

        File keyCertificateChainFile = sslDirectory.resolve("localhost.crt").toFile();
//        File keyFile = sslDirectory.resolve("localhost.key").toFile();
        File keyFile = sslDirectory.resolve("localhost.pem").toFile(); //for gRPC

        SslContext sslContext = GrpcSslContexts.configure(
                SslContextBuilder.forServer(keyCertificateChainFile, keyFile)
        ).build();

        Server server = NettyServerBuilder
                .forPort(6363)
                .sslContext(sslContext)
                .addService(new BankService(accountDatabase))
                .build();

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("gRPC server is shutting down!");
            server.shutdown();
        }));

        server.start();

        log.debug("Server started: {}", server);

        server.awaitTermination();

//        server.shutdown();
    }
}
