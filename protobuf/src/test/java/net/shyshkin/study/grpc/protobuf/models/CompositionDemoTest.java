package net.shyshkin.study.grpc.protobuf.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositionDemoTest {

    @Test
    void compositionDemo() {
        //when
        Address address = Address.newBuilder()
                .setCountry("Ukraine")
                .setCity("Kramatorsk")
                .setAddressLine1("Nekrasova str.")
                .setAddressLine2("15")
                .setZipCode(84300)
                .build();

        Car.Builder carBuilder = Car.newBuilder()
                .setBrand("Daewoo")
                .setModel("Lanos")
                .setYear(2008);

        Person person = Person.newBuilder()
                .setName("Art")
                .setAge(38)
                .setAddress(address)
                .setCar(carBuilder)
                .build();
        //then
        assertAll(
                () -> assertEquals("Art", person.getName()),
                () -> assertEquals(38, person.getAge()),
                () -> assertEquals(address, person.getAddress()),
                () -> assertEquals("Daewoo", person.getCar().getBrand()),
                () -> assertEquals("Lanos", person.getCar().getModel()),
                () -> assertEquals(2008, person.getCar().getYear())
        );
    }
}