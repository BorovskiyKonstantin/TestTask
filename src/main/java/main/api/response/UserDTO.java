package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import main.model.enums.RoleType;

import java.util.List;

public class UserDTO implements ResponseAPI {
    private String login;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<RoleType> roles;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoleType> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleType> roles) {
        this.roles = roles;
    }
}
