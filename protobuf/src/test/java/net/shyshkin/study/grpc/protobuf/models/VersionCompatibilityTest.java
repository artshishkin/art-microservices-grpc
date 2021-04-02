package net.shyshkin.study.grpc.protobuf.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionCompatibilityTest {

    @Test
    void v1_test() throws IOException {
        //given
        Television television = Television.newBuilder()
                .setBrand("sony")
                .setYear(2005)
                .build();

        //when
        Path path = Paths.get("tv-v1");
        Files.write(path, television.toByteArray());

        byte[] bytes = Files.readAllBytes(path);
        Television savedTelevision = Television.parseFrom(bytes);

        //then
        assertEquals(television, savedTelevision);
    }
}