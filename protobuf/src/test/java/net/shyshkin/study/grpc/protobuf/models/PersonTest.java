package net.shyshkin.study.grpc.protobuf.models;

import com.google.protobuf.Int32Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonTest {

    @Test
    void personBuilderDemo() {
        //when
        Person person = Person.newBuilder()
                .setName("Art")
                .setAge(Int32Value.newBuilder().setValue(38).build())
                .build();
        //then
        assertAll(
                () -> assertEquals("Art", person.getName()),
                () -> assertEquals(38, person.getAge().getValue())
        );
    }

    @Test
    @DisplayName("Persons with same field values must match")
    void equalsAndHashcodeTest() {
        //given
        Person person1 = Person.newBuilder()
                .setName("Art")
                .setAge(Int32Value.newBuilder().setValue(38).build())
                .build();

        //when
        Person person2 = Person.newBuilder()
                .setName("Art")
                .setAge(Int32Value.newBuilder().setValue(38).build())
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
                .setAge(Int32Value.newBuilder().setValue(38).build())
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