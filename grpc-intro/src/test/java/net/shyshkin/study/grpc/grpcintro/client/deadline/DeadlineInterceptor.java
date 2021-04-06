package net.shyshkin.study.grpc.grpcintro.client.deadline;

import io.grpc.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DeadlineInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        Deadline deadline = callOptions.getDeadline();
        if (Objects.isNull(deadline)) {
            callOptions = callOptions.withDeadlineAfter(700, TimeUnit.MILLISECONDS);
        }
        return next.newCall(method, callOptions);
    }
}
