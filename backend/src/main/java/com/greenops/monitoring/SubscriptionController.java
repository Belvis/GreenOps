package com.greenops.monitoring;

import com.greenops.auth.User;
import com.greenops.monitoring.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping
    public List<SubscriptionResponse> list(@AuthenticationPrincipal User user) {
        return service.list(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@AuthenticationPrincipal User user,
                                       @Valid @RequestBody SubscriptionRequest req) {
        return service.create(user.getId(), req);
    }

    @PatchMapping("/{id}")
    public SubscriptionResponse update(@AuthenticationPrincipal User user, @PathVariable Long id,
                                       @Valid @RequestBody SubscriptionUpdateRequest req) {
        return service.update(user.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.delete(user.getId(), id);
    }
}
