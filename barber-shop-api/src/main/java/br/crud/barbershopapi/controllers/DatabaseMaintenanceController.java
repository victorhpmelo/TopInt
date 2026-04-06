package br.crud.barbershopapi.controllers;

import br.crud.barbershopapi.services.DatabaseBackupService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/database")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DatabaseMaintenanceController {

    private final DatabaseBackupService databaseBackupService;

    @PostMapping("/backup")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, String> backup() throws IOException, InterruptedException {
        final var path = databaseBackupService.runManualBackup();
        return Map.of("fileName", path.getFileName().toString(), "path", path.toString());
    }

    @GetMapping("/backups")
    List<String> listBackups() throws IOException {
        return databaseBackupService.listBackups();
    }

    @PostMapping("/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void restore(@RequestParam("file") final MultipartFile file) throws IOException, InterruptedException {
        databaseBackupService.restore(file);
    }
}
