package com.rgbnet.provider.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "O nome de usuário é obrigatório")
    private String username;
    
    @NotBlank(message = "A senha é obrigatória")
    private String password;
} 