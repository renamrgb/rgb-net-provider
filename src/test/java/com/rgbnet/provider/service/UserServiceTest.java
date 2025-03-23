package com.rgbnet.provider.service;

import com.rgbnet.provider.domain.User;
import com.rgbnet.provider.dto.UserDTO;
import com.rgbnet.provider.exception.ResourceNotFoundException;
import com.rgbnet.provider.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        // Configurando usuário de teste
        Set<String> roles = new HashSet<>();
        roles.add("ADMIN");
        
        user = User.builder()
                .username("testuser")
                .password("encoded_password")
                .fullName("Test User")
                .email("test@example.com")
                .phone("11999999999")
                .roles(roles)
                .build();
        
        // Configurando o ID manualmente após a construção
        try {
            // O campo id está na superclasse BaseEntity, não na classe User
            var field = user.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Configurando DTO de usuário
        userDTO = UserDTO.builder()
                .id(userId)
                .username("testuser")
                .password("password123")
                .fullName("Test User")
                .email("test@example.com")
                .phone("11999999999")
                .roles(roles)
                .build();
    }

    @Test
    @DisplayName("Deve carregar um usuário por nome de usuário com sucesso")
    void loadUserByUsernameSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        User result = (User) userService.loadUserByUsername("testuser");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Deve buscar todos os usuários")
    void findAllSuccess() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        
        List<UserDTO> result = userService.findAll();
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    @DisplayName("Deve buscar um usuário pelo ID com sucesso")
    void findByIdSuccess() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        UserDTO result = userService.findById(userId);
        
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário por ID inexistente")
    void findByIdNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findById(userId);
        });
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void createSuccess() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserDTO result = userService.create(userDTO);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(passwordEncoder, times(1)).encode(userDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void updateSuccess() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        userDTO.setFullName("Updated Name");
        
        UserDTO result = userService.update(userId, userDTO);
        
        assertNotNull(result);
        assertEquals("Updated Name", result.getFullName());
    }

    @Test
    @DisplayName("Deve marcar um usuário como inativo ao deletar")
    void deleteSuccess() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        userService.delete(userId);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }
} 