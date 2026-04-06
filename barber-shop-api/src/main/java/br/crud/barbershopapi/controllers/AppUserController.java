package br.crud.barbershopapi.controllers;

import br.crud.barbershopapi.controllers.request.CreateAppUserRequest;
import br.crud.barbershopapi.controllers.request.UpdateAppUserRequest;
import br.crud.barbershopapi.controllers.response.AppUserResponse;
import br.crud.barbershopapi.services.AppUserManagementService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AppUserController {

    private final AppUserManagementService managementService;

    @GetMapping
    List<AppUserResponse> list() {
        return managementService.list();
    }

    @GetMapping("{id}")
    AppUserResponse findById(@PathVariable final long id) {
        return managementService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AppUserResponse create(@RequestBody @Valid final CreateAppUserRequest request) {
        return managementService.create(request);
    }

    @PutMapping("{id}")
    AppUserResponse update(@PathVariable final long id, @RequestBody @Valid final UpdateAppUserRequest request) {
        return managementService.update(id, request);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable final long id) {
        managementService.delete(id);
    }
}
