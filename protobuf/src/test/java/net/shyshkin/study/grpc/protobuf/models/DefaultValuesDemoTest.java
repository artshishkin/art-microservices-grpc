package net.shyshkin.study.grpc.protobuf.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultValuesDemoTest {

    @Test
    void defaultValueDemo() {
        //when
        Person person = Person.newBuilder().build();

        //then
        assertAll(
                () -> assertDoesNotThrow(() -> person.getAddress().getCity()),
                () -> assertEquals("", person.getName()),
                () -> assertEquals(0, person.getAge()),
                () -> assertNotNull(person.getAddress()),
                () -> assertEquals(Address.newBuilder().build(), person.getAddress()),
                () -> assertEquals(0, person.getCarCount()),
                () -> assertEquals("", person.toString())
        );
    }

    @Nested
    @DisplayName("When we want to take field value we do not know it is empty or not set")
    class EmptyDefaultValueTest {


        private Person.Builder personBuilder;

        @BeforeEach
        void setUp() {
            personBuilder = Person.newBuilder();
        }

        @Test
        @DisplayName("Empty value - has* to check")
        void takeAddressLine2_whenAddressPresent_ButHasEmptyLine2() {
            //when
            Address address = Address.newBuilder()
                    .setCountry("Ukraine")
                    .setCity("Kramatorsk")
                    .setAddressLine1("Nekrasova str.")
                    .setZipCode(84300)
                    .setAddressLine1("")
                    .build();

            Person person = personBuilder
                    .setAddress(address)
                    .build();

            //then
            assertAll(
                    () -> assertEquals("", person.getAddress().getAddressLine1()),
                    () -> assertTrue(person.hasAddress())
            );
        }

        @Test
        @DisplayName("Not set value - has* to check")
        void takeAddressLine2_whenAddressAbsent() {
            //when
            Person person = personBuilder
                    .build();

            //then
            assertAll(
                    () -> assertEquals("", person.getAddress().getAddressLine1()),
                    () -> assertFalse(person.hasAddress())
            );
        }
    }
}