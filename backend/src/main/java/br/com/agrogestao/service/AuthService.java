
package br.com.agrogestao.service;

import br.com.agrogestao.auth.JwtService;
import br.com.agrogestao.domain.User;
import br.com.agrogestao.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(
            UserRepository users,
            PasswordEncoder encoder,
            JwtService jwt) {

        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public Map<String, Object> register(
            String email,
            String password,
            String farmName) {

        String normalized = email.trim().toLowerCase();

        if (users.findByEmailIgnoreCase(normalized).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado."
            );
        }

        if (password.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve ter pelo menos 8 caracteres."
            );
        }

        User user = users.save(
                new User(
                        normalized,
                        encoder.encode(password),
                        farmName.trim()
                )
        );

        return Map.of(
                "token", jwt.create(user.getId(), user.getEmail()),
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "farmName", user.getFarmName()
                )
        );
    }

    public Map<String, Object> login(String email, String password) {

        User user = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "E-mail ou senha inválidos."
                        )
                );

        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "E-mail ou senha inválidos."
            );
        }

        return Map.of(
                "token", jwt.create(user.getId(), user.getEmail()),
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "farmName", user.getFarmName()
                )
        );
    }
}
