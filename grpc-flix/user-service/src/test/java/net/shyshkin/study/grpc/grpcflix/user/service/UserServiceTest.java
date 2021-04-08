package net.shyshkin.study.grpc.grpcflix.user.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.shyshkin.study.grpc.grpcflix.common.Genre;
import net.shyshkin.study.grpc.grpcflix.user.UserResponse;
import net.shyshkin.study.grpc.grpcflix.user.UserSearchRequest;
import net.shyshkin.study.grpc.grpcflix.user.UserServiceGrpc;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.boot.test.context.SpringBootTest;

import static io.grpc.Status.FAILED_PRECONDITION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceTest {

    private static UserServiceGrpc.UserServiceBlockingStub blockingStub;
    private static ManagedChannel managedChannel;

    @BeforeAll
    static void beforeAll() {
        managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 6565)
                .usePlaintext()
                .build();

        blockingStub = UserServiceGrpc.newBlockingStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        managedChannel.shutdown();
    }

    @Test
    void getUserGenre() {
        //given
        String loginId = "arina.climb";
        UserSearchRequest request = UserSearchRequest.newBuilder()
                .setLoginId(loginId)
                .build();

        //when
        UserResponse response = blockingStub.getUserGenre(request);

        //then
        assertThat(response)
                .hasFieldOrPropertyWithValue("genre", Genre.FANTASY)
                .hasFieldOrPropertyWithValue("loginId", loginId)
                .hasFieldOrPropertyWithValue("name", "Arina Shyshkina");
    }

    @Test
    void getUserGenre_absent() {
        //given
        String loginId = "absent.user";
        UserSearchRequest request = UserSearchRequest.newBuilder()
                .setLoginId(loginId)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            UserResponse response = blockingStub.getUserGenre(request);
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(ex -> assertThat(Status.fromThrowable(ex))
                        .hasFieldOrPropertyWithValue("code", FAILED_PRECONDITION.getCode())
                        .hasFieldOrPropertyWithValue("description", String.format("User with id `%s` not found", loginId))
                );
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvFileSource(resources = "/user.csv", numLinesToSkip = 1)
    void getUserGenreCSV(String loginId, String name, Genre genre) {
        //given

        UserSearchRequest request = UserSearchRequest.newBuilder()
                .setLoginId(loginId)
                .build();

        //when
        UserResponse response = blockingStub.getUserGenre(request);

        //then
        assertThat(response)
                .hasFieldOrPropertyWithValue("genre", genre)
                .hasFieldOrPropertyWithValue("loginId", loginId)
                .hasFieldOrPropertyWithValue("name", name);
    }
}