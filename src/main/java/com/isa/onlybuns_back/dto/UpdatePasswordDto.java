package com.isa.onlybuns_back.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePasswordDto {
    @NotNull
    private Long userId;
    @NotEmpty
    private String oldPassword;
    @NotEmpty
    private String newPassword;
    @NotEmpty
    private String confirmNewPassword;

    @AssertTrue(message = "New password and password confirmation must match")
    public boolean isPasswordsMatching() {
        if (newPassword == null || confirmNewPassword == null) return false;
        return newPassword.equals(confirmNewPassword);
    }
}
