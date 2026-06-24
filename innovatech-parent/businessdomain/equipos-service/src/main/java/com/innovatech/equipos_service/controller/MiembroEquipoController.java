package com.innovatech.equipos_service.controller;

import com.innovatech.equipos_service.dto.MiembroAuthResponse;
import com.innovatech.equipos_service.dto.MiembroEquipoRequest;
import com.innovatech.equipos_service.dto.MiembroEquipoResponse;
import com.innovatech.equipos_service.service.MiembroEquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/equipos/miembros")
public class MiembroEquipoController {

    private final MiembroEquipoService miembroEquipoService;

    @GetMapping
    public List<MiembroEquipoResponse> listarMiembros() {
        return miembroEquipoService.listarMiembros();
    }

    @GetMapping("/{id}")
    public MiembroEquipoResponse obtenerMiembroPorId(@PathVariable Long id) {
        return miembroEquipoService.obtenerMiembroPorId(id);
    }

    @GetMapping("/auth/{email}")
    public MiembroAuthResponse obtenerMiembroAuthPorEmail(@PathVariable String email) {
        return miembroEquipoService.obtenerMiembroAuthPorEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MiembroEquipoResponse crearMiembro(
            @Valid @RequestBody MiembroEquipoRequest request
    ) {
        return miembroEquipoService.crearMiembro(request);
    }

    @PutMapping("/{id}")
    public MiembroEquipoResponse actualizarMiembro(
            @PathVariable Long id,
            @Valid @RequestBody MiembroEquipoRequest request
    ) {
        return miembroEquipoService.actualizarMiembro(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivarMiembro(@PathVariable Long id) {
        miembroEquipoService.desactivarMiembro(id);
    }
}
