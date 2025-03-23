package com.rgbnet.provider.repository;

import com.rgbnet.provider.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
            
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Configurar usuário de teste
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .fullName("Test User")
                .email("test@example.com")
                .phone("11999999999")
                .roles(roles)
                .build();
    }

    @AfterEach
    void tearDown() {
        // Limpar repositório após cada teste
        userRepository.deleteAll();
    }

    @Test
    void findByUsernameSuccess() {
        // Arrange
        userRepository.save(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("Test User", foundUser.get().getFullName());
    }

    @Test
    void findByUsernameNotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByEmailSuccess() {
        // Arrange
        userRepository.save(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }
    
    @Test
    void findAllActiveUsers() {
        // Arrange
        userRepository.save(testUser);
        
        // Criar um usuário inativo
        User inactiveUser = User.builder()
                .username("inactive")
                .password("password")
                .fullName("Inactive User")
                .email("inactive@example.com")
                .roles(new HashSet<>())
                .build();
        inactiveUser.setActive(false);
        userRepository.save(inactiveUser);
        
        // Act
        List<User> activeUsers = userRepository.findByActiveTrue();
        
        // Assert
        assertFalse(activeUsers.isEmpty());
        assertEquals(1, activeUsers.size());
        assertEquals("testuser", activeUsers.get(0).getUsername());
    }
} 