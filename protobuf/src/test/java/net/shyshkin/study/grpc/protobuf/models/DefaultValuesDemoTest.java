package net.shyshkin.study.grpc.protobuf.models;

import com.google.protobuf.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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
                () -> assertEquals(0, person.getAge().getValue()),
                () -> assertFalse(person.hasAge()),
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
        void takeAddressLine1_whenAddressPresent_ButHasEmptyLine1() {
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
        void takeAddressLine1_whenAddressAbsent() {
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

    @Nested
    @DisplayName("When we want to take field value of WRAPPER type we can use has* method")
    class WrapperTypeEmptyDefaultValueTest {

        private Address.Builder addressBuilder;

        @BeforeEach
        void setUp() {

            addressBuilder = Address.newBuilder()
                    .setCountry("Ukraine")
                    .setCity("Kramatorsk")
                    .setAddressLine1("Nekrasova str.")
                    .setZipCode(84300);
        }

        @Test
        @DisplayName("Empty value - has* to check")
        void takeAddressLine2_whenPresent_ButEmpty() {
            //when
            Address address = addressBuilder
                    .setAddressLine2(StringValue.newBuilder().build())
                    .build();

            //then
            assertAll(
                    () -> assertEquals("", address.getAddressLine2().getValue()),
                    () -> assertTrue(address.hasAddressLine2())
            );
        }

        @Test
        @DisplayName("Not set value - has* to check")
        void takeAddressLine2_whenAbsent() {
            //given
            Address address = addressBuilder.build();

            //then
            assertAll(
                    () -> assertEquals("", address.getAddressLine2().getValue()),
                    () -> assertFalse(address.hasAddressLine2())
            );
        }

        @Test
        @DisplayName("Null value - NullPointerException")
        void takeAddressLine2_whenPresent_ButNull() {
            //when
            Executable settingNullExecution = () -> {
                Address address = addressBuilder
                        .setAddressLine2((StringValue) null)
                        .build();
            };

            //then
            assertThrows(NullPointerException.class, settingNullExecution);
        }
    }
}