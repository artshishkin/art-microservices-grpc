package net.shyshkin.study.grpc.protobuf.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PersonPerformanceTest {

    @Test
    void compare_JSON_gRPC_performance() {

        //when
        long jsonDuration = measureDuration(jsonRunnable);
        long grpcDuration = measureDuration(grpcRunnable);

        //then
        System.out.printf("JSON Duration: %d ms\ngRPC Duration: %d ms\n", jsonDuration, grpcDuration);
        assertTrue(5*grpcDuration < jsonDuration);
    }

    private long measureDuration(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - start;
    }

    Runnable grpcRunnable = () -> {
        try {
            //given
            Person person = Person.newBuilder()
                    .setName("Art")
                    .setAge(38)
                    .build();

            Path tempFile = Files.createTempFile("person", "ser");
            Files.write(tempFile, person.toByteArray());

            //when
            byte[] personFileContent = Files.readAllBytes(tempFile);
            Person savedPerson = Person.parseFrom(personFileContent);

            //then
            assertEquals(person, savedPerson);
            Files.delete(tempFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    Runnable jsonRunnable = () -> {
        try {
            //given
            JPerson person = new JPerson("Art", 38);

            Path tempFile = Files.createTempFile("person", "ser");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(tempFile.toFile(), person);

            //when
            JPerson savedPerson = objectMapper.readValue(tempFile.toFile(), JPerson.class);

            //then
            assertEquals(person, savedPerson);
            Files.delete(tempFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

}