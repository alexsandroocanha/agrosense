
package br.com.agrogestao.web;

import br.com.agrogestao.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    public record Register(
            @Email
            @NotBlank
            String email,

            @Size(min = 8, max = 100)
            String password,

            @NotBlank
            @Size(max = 120)
            String farmName
    ) {
    }

    public record Login(
            @Email
            @NotBlank
            String email,

            @NotBlank
            String password
    ) {
    }

    @PostMapping("/register")
    public Map<String, Object> register(
            @Valid @RequestBody Register request) {

        return service.register(
                request.email(),
                request.password(),
                request.farmName()
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @Valid @RequestBody Login request) {

        return service.login(
                request.email(),
                request.password()
        );
    }
}
