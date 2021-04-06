package net.shyshkin.study.grpc.grpcintro.client.loadbalancing;

import lombok.Getter;
import lombok.Setter;
import net.shyshkin.study.grpc.grpcintro.models.Balance;

@Setter
@Getter
class TestResultWrapper {
    Balance balance;
}
