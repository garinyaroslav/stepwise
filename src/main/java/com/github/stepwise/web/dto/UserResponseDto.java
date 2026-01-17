package com.github.stepwise.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.stepwise.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phoneNumber;
    private String address;

    public static UserResponseDto fromUser(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getProfile().getFirstName())
                .lastName(user.getProfile().getLastName())
                .middleName(user.getProfile().getMiddleName())
                .build();
    }

    public static UserResponseDto fromUserWithFullInfo(User user) {
        UserResponseDto dto = fromUser(user);
        dto.setPhoneNumber(user.getProfile().getPhoneNumber());
        dto.setAddress(user.getProfile().getAddress());
        return dto;
    }

    public static UserResponseDto fromIdAndRole(Long id, String role) {
        return UserResponseDto.builder()
                .id(id)
                .role(role)
                .build();
    }

    public static UserResponseDto fromCredentials(String username, String email) {
        return UserResponseDto.builder()
                .username(username)
                .email(email)
                .build();
    }
}
