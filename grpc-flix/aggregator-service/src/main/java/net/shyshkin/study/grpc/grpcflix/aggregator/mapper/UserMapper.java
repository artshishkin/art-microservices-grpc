package net.shyshkin.study.grpc.grpcflix.aggregator.mapper;

import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserDto;
import net.shyshkin.study.grpc.grpcflix.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    
    UserDto toUserDto(UserResponse userResponse);
    
}
