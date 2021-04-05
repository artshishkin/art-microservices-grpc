package net.shyshkin.study.grpc.grpcintro.server.rpctypes;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.AccountBalance;
import net.shyshkin.study.grpc.grpcintro.models.TransferRequest;
import net.shyshkin.study.grpc.grpcintro.models.TransferResponse;
import net.shyshkin.study.grpc.grpcintro.models.TransferStatus;

@Slf4j
@RequiredArgsConstructor
class TransferRequestStreamObserver implements StreamObserver<TransferRequest> {

    private final StreamObserver<TransferResponse> responseObserver;
    private final AccountDatabase accountDatabase;

    @Override
    public void onNext(TransferRequest transferRequest) {

        log.debug("Transfer Request '{}'", transferRequest);

        int fromAccount = transferRequest.getFromAccount();
        int toAccount = transferRequest.getToAccount();
        int amount = transferRequest.getAmount();
        int availableBalance = accountDatabase.getBalance(fromAccount);
        TransferStatus status = TransferStatus.FAILED;
        int fromBalance = availableBalance;
        int toBalance = accountDatabase.getBalance(toAccount);
        if (fromAccount != toAccount && availableBalance >= amount) {
            fromBalance = accountDatabase.deductBalance(fromAccount, amount);
            toBalance = accountDatabase.addBalance(toAccount, amount);
            status = TransferStatus.SUCCESS;
        }
        TransferResponse response = TransferResponse.newBuilder()
                .addAccountBalances(AccountBalance.newBuilder().setAccountNumber(fromAccount).setAmount(fromBalance))
                .addAccountBalances(AccountBalance.newBuilder().setAccountNumber(toAccount).setAmount(toBalance))
                .setStatus(status)
                .build();
        log.debug("Transfer finished with response: `{}`", transferResponseToString(response));
        responseObserver.onNext(response);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Exception happened", t);
    }

    @Override
    public void onCompleted() {
        log.debug("onCompleted");
        responseObserver.onCompleted();
    }

    private String transferResponseToString(TransferResponse transferResponse) {
        StringBuilder builder = new StringBuilder();
        builder.append(" [").append(transferResponse.getStatus()).append("] ");
        transferResponse.getAccountBalancesList()
                .forEach(accountBalance -> builder
                        .append("{")
                        .append(accountBalance.getAccountNumber())
                        .append(" : ")
                        .append(accountBalance.getAmount())
                        .append("}")
                );
        return builder.toString();
    }
}
