package main.api.response;

import java.util.List;

public class UserListResponse implements ResponseAPI {
    private long count;
    private List<UserDTO> users;

    public UserListResponse(long count, List<UserDTO> users) {
        this.count = count;
        this.users = users;
    }

    public long getCount() {
        return count;
    }

    public List<UserDTO> getUsers() {
        return users;
    }
}
