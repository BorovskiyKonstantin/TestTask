package main.service;

import main.api.request.UserDataRequest;
import main.api.response.BooleanResponseDTO;
import main.api.response.ResponseAPI;
import main.api.response.UserDTO;
import main.api.response.UserListResponse;
import main.model.Role;
import main.model.User;
import main.model.enums.RoleType;
import main.repository.RoleRepository;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public ResponseEntity<ResponseAPI> createUser(UserDataRequest registerDto) {
        // Проверка пришедших значений на ошибки
        Map<String, String> errors = checkErrors(registerDto);  //проверка полей запроса на ошибки
        if (userRepository.findById(registerDto.getLogin()).isPresent()) //проверка в БД занят ли логин
            errors.put("login", "login already exists");
        if (errors.size() > 0)
            return new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        // Найти переданные роли в БД
        List<Role> rolesFromDB = null;
        if (registerDto.getRoles() != null) {
            rolesFromDB = new ArrayList<>();
            for (String role : registerDto.getRoles()) {
                rolesFromDB.add(roleRepository.findByName(role).orElseThrow());
            }
        }

        User user = new User(
                registerDto.getLogin(),
                registerDto.getName(),
                registerDto.getPassword(),
                rolesFromDB);
        userRepository.save(user);

        return new ResponseEntity<>(new BooleanResponseDTO(true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseAPI> updateUser(String login, UserDataRequest editDto) {
        // Найти редактируемого User в БД по login или ответ со статусом 404
        User userFromDB = userRepository.findById(login).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Проверка пришедших значений на ошибки
        Map<String, String> errors = checkErrors(editDto);  //проверка полей запроса на ошибки
        // В случае изменения логина удалить старую запись из БД и работать с новым User
        if (!login.equals(editDto.getLogin())) {
            if (userRepository.findById(editDto.getLogin()).isPresent()) //проверка в БД занят ли логин
                errors.put("login", "login already exists");
            else {
                userRepository.delete(userFromDB);
                userFromDB = new User();
            }
        }

        if (errors.size() > 0)
            return new ResponseEntity<>(new BooleanResponseDTO(errors), HttpStatus.OK);

        // Найти переданные роли в БД
        List<Role> rolesFromDB = null;
        if (editDto.getRoles() != null) {
            rolesFromDB = new ArrayList<>();
            for (String role : editDto.getRoles()) {
                rolesFromDB.add(roleRepository.findByName(role).orElseThrow());
            }
        }

        //Редактирование и сохранение User
        userFromDB.setLogin(editDto.getLogin());
        userFromDB.setName(editDto.getName());
        userFromDB.setPassword(editDto.getPassword());
        userFromDB.setRoles(rolesFromDB);
        userRepository.save(userFromDB);

        return new ResponseEntity<>(new BooleanResponseDTO(true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseAPI> getAll() {
        List<UserDTO> userDtoList = userRepository.findAll()
                .stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setLogin(user.getLogin());
                    dto.setName(user.getName());
                    return dto;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<>(new UserListResponse(userDtoList.size(), userDtoList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseAPI> getUser(String login) {
        // Найти редактируемого User в БД по login или ответ со статусом 404
        User user = userRepository.findById(login).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserDTO dto = new UserDTO();
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setRoles(user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toList())
        );
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseAPI> deleteUser(String login) {
        userRepository.deleteByLogin(login);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Map<String, String> checkErrors(UserDataRequest dto) {
        List<String> roles = dto.getRoles();
        Map<String, String> errors = new LinkedHashMap<>();

        if (!checkStringField(dto.getLogin()))
            errors.put("login", "invalid argument");
        if (!checkStringField(dto.getName()))
            errors.put("name", "invalid argument");
        if (!checkStringField(dto.getPassword())
                || !dto.getPassword().matches(".*[A-Z]+.*")
                || !dto.getPassword().matches(".*\\d+.*"))
            errors.put("password", "invalid argument");
        //для проверки соответствия переданных имён ролей с существующими enum RoleType
        if (roles != null) {
            try {
                for (String role : roles) {
                    RoleType.valueOf(role);
                }
            } catch (IllegalArgumentException e) {
                errors.put("roles", "invalid argument");
            }
        }

        return errors;
    }

    //Общие для всех полей проверки
    private boolean checkStringField(String field) {
        if (field == null) return false;
        if (field.length() <= 3) return false;
        return field.replaceAll("[A-z0-9]+", "").length() <= 0;
    }
}
