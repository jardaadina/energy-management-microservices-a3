package com.energy.device.controller;

import com.energy.device.dto.CreateDeviceRequest;
import com.energy.device.dto.DeviceDTO;
import com.energy.device.dto.UpdateDeviceRequest;
import com.energy.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    private void checkAdmin(String role)
    {
        if (!"ADMIN".equalsIgnoreCase(role))
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access Denied: Requires ADMIN role.");
        }
    }

    @Operation(summary = "Creează un dispozitiv nou", description = "Adaugă un dispozitiv nou în sistem. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dispozitiv creat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content)
    })

    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody CreateDeviceRequest request,
                                                  @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to create device: {}", request.getName());
        DeviceDTO device = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @Operation(summary = "Obține un dispozitiv după ID", description = "Returnează un singur dispozitiv, pe baza ID-ului. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispozitiv găsit",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dispozitiv negăsit", content = @Content)
    })

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Long id,
                                                   @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to get device: {}", id);
        DeviceDTO device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    @Operation(summary = "Obține toate dispozitivele", description = "Returnează o listă cu toate dispozitivele din sistem. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista dispozitivelor",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content)
    })

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices(@RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to get all devices");
        List<DeviceDTO> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    @Operation(summary = "Actualizează un dispozitiv", description = "Actualizează datele unui dispozitiv. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispozitiv actualizat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dispozitiv negăsit", content = @Content)
    })

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeviceRequest request,
            @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to update device: {}", id);
        DeviceDTO device = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(device);
    }

    @Operation(summary = "Șterge un dispozitiv", description = "Șterge un dispozitiv din sistem. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dispozitiv șters cu succes", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dispozitiv negăsit", content = @Content)
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id,
                                             @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to delete device: {}", id);
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}