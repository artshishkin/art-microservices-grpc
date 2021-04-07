package net.shyshkin.study.grpc.grpcintro.server.rpctypes;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AccountDatabase {

    private final Map<Integer, Integer> DB = IntStream
            .rangeClosed(1, 100)
            .boxed()
            .collect(
                    Collectors.toMap(
                            Function.identity(),
                            v -> v * 111
                    )
            );

    public int getBalance(int accountId) {
        return DB.get(accountId);
    }

    public int addBalance(int accountId, int amount) {
        return DB.computeIfPresent(accountId, (k, v) -> v + amount);
    }

    public int deductBalance(int accountId, int amount) {
        return DB.computeIfPresent(accountId, (k, v) -> v - amount);
    }

    public boolean accountPresent(int accountId) {
        return DB.containsKey(accountId);
    }
}
