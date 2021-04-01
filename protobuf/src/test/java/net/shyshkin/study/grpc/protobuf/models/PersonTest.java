package net.shyshkin.study.grpc.protobuf.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @Test
    @DisplayName("Persons with same field values must match")
    void equalsAndHashcodeTest() {
        //given
        Person person1 = Person.newBuilder()
                .setName("Art")
                .setAge(38)
                .build();

        //when
        Person person2 = Person.newBuilder()
                .setName("Art")
                .setAge(38)
                .build();

        //then
        assertEquals(person1, person2);
        assertEquals(person1.hashCode(), person2.hashCode());
    }

    @Test
    void serializationDeserialization() throws IOException {
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
    }
}