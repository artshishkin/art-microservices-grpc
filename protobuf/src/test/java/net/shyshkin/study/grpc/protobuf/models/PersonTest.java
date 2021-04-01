package net.shyshkin.study.grpc.protobuf.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Test
    void personBuilderDemo() {
        //when
        Person person = Person.newBuilder()
                .setName("Art")
                .setAge(38)
                .build();
        //then
        assertAll(
                () -> assertEquals("Art", person.getName()),
                () -> assertEquals(38, person.getAge())
        );
    }
}