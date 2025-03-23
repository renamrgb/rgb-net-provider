package com.rgbnet.provider.service;

import com.rgbnet.provider.domain.User;
import com.rgbnet.provider.dto.UserDTO;
import com.rgbnet.provider.exception.ResourceNotFoundException;
import com.rgbnet.provider.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        // Limpa o banco antes de cada teste
        userRepository.deleteAll();
    }
    
    @AfterEach
    void tearDown() {
        // Limpa o banco após cada teste
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Deve criar um usuário com sucesso e recuperá-lo pelo ID")
    void createAndRetrieveUser() {
        // Arrange
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        
        UserDTO userDTO = UserDTO.builder()
                .username("testuser")
                .password("password123")
                .fullName("Test User")
                .email("test@example.com")
                .phone("11999999999")
                .roles(roles)
                .build();
        
        // Act
        UserDTO createdUser = userService.create(userDTO);
        
        // Assert
        assertNotNull(createdUser.getId(), "O ID do usuário não deve ser nulo");
        
        // Busca o usuário pelo ID para verificar se foi persistido corretamente
        UserDTO retrievedUser = userService.findById(createdUser.getId());
        
        assertNotNull(retrievedUser);
        assertEquals(createdUser.getUsername(), retrievedUser.getUsername());
        assertEquals(createdUser.getFullName(), retrievedUser.getFullName());
        assertEquals(createdUser.getEmail(), retrievedUser.getEmail());
    }
    
    @Test
    @DisplayName("Deve buscar todos os usuários")
    void findAllUsers() {
        // Arrange - Cria alguns usuários para testar
        createTestUser("user1", "user1@example.com");
        createTestUser("user2", "user2@example.com");
        
        // Act
        List<UserDTO> users = userService.findAll();
        
        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário por ID inexistente")
    void findByIdNotFound() {
        // Act & Assert
        UUID randomId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findById(randomId);
        });
    }
    
    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void updateUserSuccess() {
        // Arrange
        UserDTO userDTO = createTestUser("updateuser", "update@example.com");
        
        // Act
        userDTO.setFullName("Nome Atualizado");
        UserDTO updatedUser = userService.update(userDTO.getId(), userDTO);
        
        // Assert
        assertEquals("Nome Atualizado", updatedUser.getFullName());
        
        // Verifica se a atualização foi persistida
        UserDTO retrievedUser = userService.findById(userDTO.getId());
        assertEquals("Nome Atualizado", retrievedUser.getFullName());
    }
    
    // Método auxiliar para criar usuários de teste
    private UserDTO createTestUser(String username, String email) {
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        
        UserDTO userDTO = UserDTO.builder()
                .username(username)
                .password("password123")
                .fullName("Test User " + username)
                .email(email)
                .phone("11999999999")
                .roles(roles)
                .build();
        
        return userService.create(userDTO);
    }
} 