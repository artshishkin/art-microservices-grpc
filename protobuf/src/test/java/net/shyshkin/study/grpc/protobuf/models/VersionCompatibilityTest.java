package net.shyshkin.study.grpc.protobuf.models;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionCompatibilityTest {

    @Test
    @Disabled("Was for testing in `32. Proto - API/Message Changes - Part - 1` and creation of `tv-v1` file")
    void v1_test() throws IOException {
        //given
        Television television = Television.newBuilder()
                .setBrand("sony")
//                .setYear(2005)
                .build();

        //when
        Path path = Paths.get("tv-v1");
        Files.write(path, television.toByteArray());

        byte[] bytes = Files.readAllBytes(path);
        Television savedTelevision = Television.parseFrom(bytes);

        //then
        assertEquals(television, savedTelevision);
    }

    @Test
    void v2_test_read_v1() throws IOException {
        //given
        Path path = Paths.get("tv-v1");

        //when
        byte[] bytes = Files.readAllBytes(path);
        Television savedTelevision = Television.parseFrom(bytes);

        //then
        System.out.println(savedTelevision);
        assertAll(
                () -> assertEquals("sony", savedTelevision.getBrand()),
                () -> assertEquals(2005, savedTelevision.getModel()),
                () -> assertEquals(Type.UNDEFINED, savedTelevision.getType())
        );
    }

    @Test
    void v2_test_create_v2() throws IOException {
        //given
        Television television = Television.newBuilder()
                .setBrand("sony")
                .setModel(12345)
                .setType(Type.OLED)
                .build();

        //when
        Path path = Paths.get("tv-v2");
        Files.write(path, television.toByteArray());

        byte[] bytes = Files.readAllBytes(path);
        Television savedTelevision = Television.parseFrom(bytes);

        //then
        System.out.println(savedTelevision);
        assertEquals(television, savedTelevision);
    }
}