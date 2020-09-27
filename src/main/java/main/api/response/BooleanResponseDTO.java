package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
public class BooleanResponseDTO implements ResponseAPI {
    private Boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> errors;

    public BooleanResponseDTO(boolean success) {
        this.success = success;
    }

    public BooleanResponseDTO(Map<String, String> errors) {
        this.success = errors.size() == 0;
        this.errors = errors;
    }
}
