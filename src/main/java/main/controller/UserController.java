package main.controller;

import main.api.request.UserDataRequest;
import main.api.response.ResponseAPI;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/")
public class UserController {
    @Autowired
    private UserService userService;

    //1. Получать список пользователей из БД (без ролей)
    @GetMapping()
    public ResponseEntity<ResponseAPI> getAllUsers() {
        return userService.getAll();
    }

    //2. Получать конкретного пользователя (с его ролями) из БД
    @GetMapping(value = "{login}")
    public ResponseEntity<ResponseAPI> getUser(@PathVariable("login") String login) {
        return userService.getUser(login);
    }

    //3. Удалять пользователя в БД
    @DeleteMapping(value = "{login}")
    public ResponseEntity<ResponseAPI> deleteUser(@PathVariable("login") String login) {
        return userService.deleteUser(login);
    }

    //4. Добавлять нового пользователя с ролями в БД.
    @PostMapping(value = "add/")
    public ResponseEntity<ResponseAPI> addUser(@RequestBody UserDataRequest registerDto) {
        return userService.createUser(registerDto);
    }

    //5. Редактировать существующего пользователя в БД.
    // Если в запросе на редактирование передан массив ролей, система должна обновить
    // список ролей пользователя в БД - новые привязки добавить, неактуальные привязки удалить.
    @PutMapping(value = "{login}")
    public ResponseEntity<ResponseAPI> editUser(@PathVariable("login") String login,
                                                @RequestBody UserDataRequest editDto) {
        return userService.updateUser(login, editDto);
    }
}
