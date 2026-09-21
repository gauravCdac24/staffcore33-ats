package com.staffcore33.ats.client;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) Long accountManagerId,
            @RequestParam(required = false) String search) {
        return clientService.list(status, industry, accountManagerId, search);
    }

    @GetMapping("/deleted")
    public List<ClientResponse> listDeleted() {
        return clientService.listDeleted();
    }

    @GetMapping("/{id}")
    public ClientResponse get(@PathVariable Long id) {
        return clientService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@Valid @RequestBody ClientRequest request) {
        return clientService.create(request);
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return clientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        clientService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    public ClientResponse restore(@PathVariable Long id) {
        return clientService.restore(id);
    }
}
