package net.shyshkin.study.grpc.protobuf.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Int32Value;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonPerformanceSimpleTest {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void compare_JSON_gRPC_performance() {
        //given
        Person person = Person.newBuilder()
                .setName("Art")
                .setAge(Int32Value.newBuilder().setValue(38).build())
                .build();

        JPerson jPerson = new JPerson("Art", 38);

        Runnable grpcRunnable = () -> {
            try {
                //when
                byte[] personBytes = person.toByteArray();
                Person deserializedPerson = Person.parseFrom(personBytes);

                //then
//                assertEquals(person, deserializedPerson);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable jsonRunnable = () -> {
            try {
                //when
                byte[] bytes = objectMapper.writeValueAsBytes(jPerson);
                JPerson deserializedPerson = objectMapper.readValue(bytes, JPerson.class);

                //then
//                assertEquals(jPerson, deserializedPerson);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        //when
        long jsonDuration = measureDurationMultiple(jsonRunnable, 1_000_000);
        long grpcDuration = measureDurationMultiple(grpcRunnable, 1_000_000);

        //then
        System.out.printf("JSON Duration: %d ms\ngRPC Duration: %d ms\n", jsonDuration, grpcDuration);
        assertTrue(2 * grpcDuration < jsonDuration);
    }

    private long measureDuration(Runnable runnable) {
        return measureDurationMultiple(runnable, 1);
    }

    private long measureDurationMultiple(Runnable runnable, int count) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            runnable.run();
        }
        return System.currentTimeMillis() - start;
    }

    @Test
    void view_JSON_PROTO_object_size() {
        //given
        Person person = Person.newBuilder()
                .setName("Art")
                .setAge(Int32Value.newBuilder().setValue(38).build())
                .build();

        JPerson jPerson = new JPerson("Art", 38);

        Runnable grpcRunnable = () -> {
            try {
                //when
                byte[] personBytes = person.toByteArray();
                System.out.printf("Size of Proto Person: %d bytes\n", personBytes.length);
                Person deserializedPerson = Person.parseFrom(personBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable jsonRunnable = () -> {
            try {
                //when
                byte[] personBytes = objectMapper.writeValueAsBytes(jPerson);
                System.out.printf("Size of JSON Person: %d bytes\n", personBytes.length);
                JPerson deserializedPerson = objectMapper.readValue(personBytes, JPerson.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        //when
        long jsonDuration = measureDurationMultiple(jsonRunnable, 1);
        long grpcDuration = measureDurationMultiple(grpcRunnable, 1);

        //then
        assertTrue(2 * grpcDuration < jsonDuration);
    }

}