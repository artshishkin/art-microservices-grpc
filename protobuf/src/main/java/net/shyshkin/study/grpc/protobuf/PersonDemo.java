package net.shyshkin.study.grpc.protobuf;

import com.google.protobuf.Int32Value;
import net.shyshkin.study.grpc.protobuf.models.Person;

public class PersonDemo {

    public static void main(String[] args) {
        Person person = Person.newBuilder()
                .setName("Art")
                .setAge(Int32Value.newBuilder().setValue(38).build())
                .build();
        System.out.println(person);
    }

}
