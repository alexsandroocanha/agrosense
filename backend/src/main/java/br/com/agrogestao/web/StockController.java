
package br.com.agrogestao.web;

import br.com.agrogestao.domain.*;
import br.com.agrogestao.repo.UserRepository;
import br.com.agrogestao.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class StockController {

    private final StockService service;
    private final UserRepository users;

    public StockController(StockService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    private UUID farm(Authentication authentication) {
        return users.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED))
                .getId();
    }

    @GetMapping("/supplies")
    public List<Supply> supplies(Authentication authentication) {
        return service.list(farm(authentication));
    }

    @PostMapping("/supplies")
    @ResponseStatus(HttpStatus.CREATED)
    public Supply create(
            Authentication authentication,
            @RequestBody @Valid StockService.SupplyInput input) {

        return service.create(farm(authentication), input);
    }

    @PutMapping("/supplies/{id}")
    public Supply update(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody @Valid StockService.SupplyInput input) {

        return service.update(farm(authentication), id, input);
    }

    @DeleteMapping("/supplies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(
            Authentication authentication,
            @PathVariable UUID id) {

        service.archive(farm(authentication), id);
    }

    @GetMapping("/movements")
    public List<Movement> movements(Authentication authentication) {
        return service.history(farm(authentication));
    }

    @GetMapping("/supplies/{id}/movements")
    public List<Movement> history(
            Authentication authentication,
            @PathVariable UUID id) {

        return service.historyFor(farm(authentication), id);
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public Movement movement(
            Authentication authentication,
            @RequestBody @Valid StockService.MovementInput input) {

        return service.movement(
                farm(authentication),
                UUID.fromString(authentication.getName()),
                input
        );
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication authentication) {
        return service.dashboard(farm(authentication));
    }

    @GetMapping("/alerts")
    public List<StockAlert> alerts(Authentication authentication) {
        return service.getAlerts(farm(authentication));
    }

    @PostMapping("/analysis/run")
    public List<StockAlert> analyze(Authentication authentication) {
        return service.reanalyze(farm(authentication));
    }
}
