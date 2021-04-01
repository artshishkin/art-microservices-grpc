package net.shyshkin.study.grpc.protobuf;

import net.shyshkin.study.grpc.protobuf.models.Person;

public class PersonDemo {

    public static void main(String[] args) {
        Person person = Person.newBuilder()
                .setName("Art")
                .setAge(38)
                .build();
        System.out.println(person);
    }

}
