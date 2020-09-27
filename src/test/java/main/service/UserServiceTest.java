package main.service;

import main.api.request.UserDataRequest;
import main.api.response.BooleanResponseDTO;
import main.api.response.ResponseAPI;
import main.model.Role;
import main.model.User;
import main.repository.RoleRepository;
import main.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Список тестов:
 * - для createUser:
 * 1. Тест с валидным DTO
 * 2. Тест с уже существующим login
 * 3. Тест с невалидным name
 * 4. Тест с невалидным login
 * 5. Тест с невалидным password
 * 6. Тест с невалидным roles
 * - для updateUser:
 * 7. Тест с валидным DTO
 * 8. Тест с изменением несуществующего пользователя
 * 9. Тест с заменой login на уже существующий в БД
 * 10. Тест с невалидным login
 * 11. Тест с невалидным name
 * 12. Тест с невалидным password
 * 13. Тест с невалидным roles
 */

/**
 * Для проверки полей запроса приняты следующие правила:
 * 1. общие для всех полей:
 * - not null
 * - содержит только латинские буквы и цифры
 * - длина строки >3 символов
 * <p>
 * 2. дополнительно для login:
 * - еще не зарегистрирован
 * <p>
 * 3. дополнительно для password:
 * - содержит букву в заглавном регистре
 * - содержит цифру
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RoleRepository roleRepository;

    private static UserDataRequest testRequestDto;

    @Before
    public void createTestRequestDto() {
        //Создание валидного DTO перед каждым тестом
        testRequestDto = new UserDataRequest();
        testRequestDto.setLogin("login");
        testRequestDto.setName("name");
        testRequestDto.setPassword("abcD1");
        testRequestDto.setRoles(List.of("USER"));
    }

    // 1. Тест с валидным DTO
    @Test
    public void createUser_ValidRequestShouldReturnTrueResponse() {
        Mockito.doReturn(Optional.of(new Role())).when(roleRepository).findByName("USER");
        ResponseEntity<ResponseAPI> actualResponse = userService.createUser(testRequestDto);
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(true), HttpStatus.OK);
        Assert.assertEquals(expectedResponse, actualResponse);
    }

    // 2. Тест с уже существующим login
    @Test
    public void createUser_ExistsLoginShouldReturnFalseResponse() {
        Mockito.doReturn(Optional.of(new User())).when(userRepository).findById("existsLogin");
        testRequestDto.setLogin("existsLogin");
        ResponseEntity<ResponseAPI> actualResponse = userService.createUser(testRequestDto);

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("login", "login already exists");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);
        Assert.assertEquals(expectedResponse, actualResponse);
    }

    // 3. Тест с невалидным login
    @Test
    public void createUser_InvalidLoginShouldReturnFalseResponse() {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("login", "invalid argument");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        String[] invalidValues = {null, "abc", "абвгд"};
        for (String invalidLogin : invalidValues) {
            testRequestDto.setLogin(invalidLogin);
            ResponseEntity<ResponseAPI> actualResponse = userService.createUser(testRequestDto);
            Assert.assertEquals(expectedResponse, actualResponse);
        }
    }

    // 4. Тест с невалидным name
    @Test
    public void createUser_InvalidNameShouldReturnFalseResponse() {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("name", "invalid argument");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        String[] invalidValues = {null, "abc", "абвгд"};
        for (String invalidName : invalidValues) {
            testRequestDto.setName(invalidName);
            ResponseEntity<ResponseAPI> actualResponse = userService.createUser(testRequestDto);
            Assert.assertEquals(expectedResponse, actualResponse);
        }
    }

    // 5. Тест с невалидным password
    @Test
    public void createUser_InvalidPasswordShouldReturnFalseResponse() {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("password", "invalid argument");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        String[] invalidValues = {null, "abc", "абвгд", "abcd", "abcD", "abc1"};
        for (String invalidPassword : invalidValues) {
            testRequestDto.setPassword(invalidPassword);
            ResponseEntity<ResponseAPI> actualResponse = userService.createUser(testRequestDto);
            Assert.assertEquals(expectedResponse, actualResponse);
        }
    }

    // 6. Тест с невалидным roles
    @Test
    public void createUser_InvalidRolesShouldReturnFalseResponse() {
        Mockito.doReturn(Optional.empty()).when(roleRepository).findByName("Invalid_Role");

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("roles", "invalid argument");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        List<String> invalidRoles = Collections.singletonList("Invalid_Role");
        testRequestDto.setRoles(invalidRoles);
        ResponseEntity<ResponseAPI> actualResponse = userService.createUser(testRequestDto);
        Assert.assertEquals(expectedResponse, actualResponse);
    }

    // 7. Тест с валидным DTO
    @Test
    public void updateUser_ValidRequestShouldReturnTrueResponse() {
        Mockito.doReturn(Optional.of(new User("currentLogin", "userName", "abcD1", List.of(new Role()))))
                .when(userRepository)
                .findById("currentLogin");
        Mockito.doReturn(Optional.empty())
                .when(userRepository)
                .findById("login");
        Mockito.doReturn(Optional.of(new Role()))
                .when(roleRepository)
                .findByName("USER");

        ResponseEntity<ResponseAPI> actualResponse = userService.updateUser("currentLogin", testRequestDto);
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(true), HttpStatus.OK);
        Assert.assertEquals(expectedResponse, actualResponse);
    }

    // 8. Тест с изменением несуществующего пользователя должен вернуть исключение со статусом NOT_FOUND
    @Test
    public void updateUser_NotExistsUserShouldReturnError404() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser("notExistsUser", testRequestDto));
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;
        HttpStatus actualStatus = ex.getStatus();
        Assert.assertEquals(expectedStatus, actualStatus);
    }

    // 9. Тест с заменой login на уже существующий в БД
    @Test
    public void updateUser_ExistsLoginShouldReturnFalseResponse() {
        Mockito.doReturn(Optional.of(new User("currentLogin", "userName", "abcD1", List.of(new Role()))))
                .when(userRepository)
                .findById("currentLogin");
        Mockito.doReturn(Optional.of(new Role()))
                .when(roleRepository)
                .findByName("USER");
        Mockito.doReturn(Optional.of(new User()))
                .when(userRepository)
                .findById("existsLogin");

        testRequestDto.setLogin("existsLogin");
        ResponseEntity<ResponseAPI> actualResponse = userService.updateUser("currentLogin", testRequestDto);

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("login", "login already exists");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);
        Assert.assertEquals(expectedResponse, actualResponse);
    }

    // 10. Тест с невалидным login
    @Test
    public void updateUser_InvalidLoginShouldReturnFalseResponse() {
        Mockito.doReturn(Optional.of(new User()))
                .when(userRepository)
                .findById("currentLogin");

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("login", "invalid argument");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        String[] invalidValues = {null, "abc", "абвгд"};
        for (String invalidLogin : invalidValues) {
            testRequestDto.setLogin(invalidLogin);
            ResponseEntity<ResponseAPI> actualResponse = userService.updateUser("currentLogin", testRequestDto);
            Assert.assertEquals(expectedResponse, actualResponse);
        }
    }

    // 11. Тест с невалидным name
    @Test
    public void updateUser_InvalidNameShouldReturnFalseResponse() {
        Mockito.doReturn(Optional.of(new User()))
                .when(userRepository)
                .findById("currentLogin");

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("name", "invalid argument");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        String[] invalidValues = {null, "abc", "абвгд"};
        for (String invalidName : invalidValues) {
            testRequestDto.setName(invalidName);
            ResponseEntity<ResponseAPI> actualResponse = userService.updateUser("currentLogin", testRequestDto);
            Assert.assertEquals(expectedResponse, actualResponse);
        }
    }

    // 12. Тест с невалидным password
    @Test
    public void updateUser_InvalidPasswordShouldReturnFalseResponse() {
        Mockito.doReturn(Optional.of(new User()))
                .when(userRepository)
                .findById("currentLogin");

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("password", "invalid argument");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        String[] invalidValues = {null, "abc", "абвгд", "abcd", "abcD", "abc1"};
        for (String invalidPassword : invalidValues) {
            testRequestDto.setPassword(invalidPassword);
            ResponseEntity<ResponseAPI> actualResponse = userService.updateUser("currentLogin", testRequestDto);
            Assert.assertEquals(expectedResponse, actualResponse);
        }
    }

    // 13. Тест с невалидным roles
    @Test
    public void updateUser_InvalidRolesShouldReturnFalseResponse() {
        Mockito.doReturn(Optional.of(new User()))
                .when(userRepository)
                .findById("currentLogin");
        Mockito.doReturn(Optional.empty()).when(roleRepository).findByName("Invalid_Role");

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("roles", "invalid argument");
        ResponseEntity<ResponseAPI> expectedResponse = new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        List<String> invalidRoles = Collections.singletonList("Invalid_Role");
        testRequestDto.setRoles(invalidRoles);
        ResponseEntity<ResponseAPI> actualResponse = userService.updateUser("currentLogin", testRequestDto);
        Assert.assertEquals(expectedResponse, actualResponse);
    }
}