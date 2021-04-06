package net.shyshkin.study.grpc.grpcintro.client.deadline;

import io.grpc.*;

import java.util.concurrent.TimeUnit;

public class DeadlineInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
//        return next.newCall(method, callOptions);  // just do nothing
        return next.newCall(method, callOptions.withDeadlineAfter(700, TimeUnit.MILLISECONDS));
    }
}
