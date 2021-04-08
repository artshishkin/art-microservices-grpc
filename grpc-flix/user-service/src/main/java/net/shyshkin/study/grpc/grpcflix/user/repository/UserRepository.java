package net.shyshkin.study.grpc.grpcflix.user.repository;

import net.shyshkin.study.grpc.grpcflix.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
