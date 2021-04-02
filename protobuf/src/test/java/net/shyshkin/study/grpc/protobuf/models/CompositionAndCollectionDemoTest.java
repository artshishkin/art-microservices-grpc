package net.shyshkin.study.grpc.protobuf.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositionAndCollectionDemoTest {

    @Test
    void collectionDemo() {
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

        Car newCar = Car.newBuilder()
                .setBrand("Ford")
                .setModel("Escape")
                .setYear(2015)
                .build();

        Person person = Person.newBuilder()
                .setName("Art")
                .setAge(38)
                .setAddress(address)
                .addCar(carBuilder)
                .addCar(1, newCar)
                .build();
        //then
        assertAll(
                () -> assertEquals("Art", person.getName()),
                () -> assertEquals(38, person.getAge()),
                () -> assertEquals(address, person.getAddress()),
                () -> assertEquals(2, person.getCarCount()),
                () -> assertEquals(newCar, person.getCarList().get(1)),
                () -> assertEquals("Daewoo", person.getCar(0).getBrand()),
                () -> assertEquals("Lanos", person.getCar(0).getModel()),
                () -> assertEquals(2008, person.getCar(0).getYear())
        );
    }

    @Test
    void mapDemo() {
        //given
        Car escape = Car.newBuilder()
                .setBrand("Ford")
                .setModel("Escape")
                .setYear(2015)
                .build();

        Car journey = Car.newBuilder()
                .setBrand("Dodge")
                .setModel("Journey")
                .setYear(2014)
                .build();

        //when
        Dealer dealer = Dealer.newBuilder()
                .putCarPosition(54321, escape)
                .putCarPosition(54322, journey)
                .build();

        //then
        assertEquals(escape, dealer.getCarPositionOrThrow(54321));
        assertEquals(2, dealer.getCarPositionCount());
        System.out.println(dealer);
    }
}