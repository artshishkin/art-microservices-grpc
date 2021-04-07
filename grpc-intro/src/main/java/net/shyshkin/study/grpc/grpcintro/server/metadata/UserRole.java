package net.shyshkin.study.grpc.grpcintro.server.metadata;

public enum UserRole {
    PRIME,      // like `admin` - has access to another accounts
    STANDARD    // ordinary `user` - has access only to his account
}
