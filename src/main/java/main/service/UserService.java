package main.service;

import main.api.request.UserDataRequest;
import main.api.response.ResponseAPI;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<ResponseAPI> createUser(UserDataRequest registerDto);

    ResponseEntity<ResponseAPI> updateUser(String login, UserDataRequest editDto);

    ResponseEntity<ResponseAPI> getAll();

    ResponseEntity<ResponseAPI> getUser(String login);

    ResponseEntity<ResponseAPI> deleteUser(String login);
}
