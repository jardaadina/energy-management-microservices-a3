package com.energy.device.controller;

import com.energy.device.dto.AssignDeviceRequest;
import com.energy.device.dto.DeviceDTO;
import com.energy.device.service.UserDeviceService;
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
@RequestMapping("/user-devices")
@RequiredArgsConstructor
@Slf4j
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    private void checkAdmin(String role)
    {
        if (!"ADMIN".equalsIgnoreCase(role))
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access Denied: Requires ADMIN role.");
        }
    }

    @Operation(summary = "Asociază un dispozitiv unui utilizator (Admin)", description = "Creează o nouă asociere între un utilizator (după ID) și un dispozitiv (după ID). Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asociere creată cu succes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class, example = "Device assigned successfully"))),
            @ApiResponse(responseCode = "400", description = "Date invalide (ex. ID-uri lipsă)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizator sau Dispozitiv negăsit", content = @Content),
            @ApiResponse(responseCode = "409", description = "Asocierea există deja (Conflict)", content = @Content)
    })
    @PostMapping("/assign")
    public ResponseEntity<String> assignDevice(@Valid @RequestBody AssignDeviceRequest request,
                                               @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to assign device {} to user {}",
                request.getDeviceId(), request.getUserId());
        userDeviceService.assignDeviceToUser(request.getUserId(), request.getDeviceId());
        return ResponseEntity.ok("Device assigned successfully");
    }

    @Operation(summary = "Dezasociază un dispozitiv de la un utilizator (Admin)", description = "Șterge o asociere existentă între un utilizator și un dispozitiv. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dezasociere reușită",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class, example = "Device unassigned successfully"))),
            @ApiResponse(responseCode = "400", description = "Date invalide (ex. ID-uri lipsă)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Asocierea nu a fost găsită", content = @Content)
    })

    @DeleteMapping("/unassign")
    public ResponseEntity<String> unassignDevice(@Valid @RequestBody AssignDeviceRequest request,
                                                 @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to unassign device {} from user {}",
                request.getDeviceId(), request.getUserId());
        userDeviceService.unassignDeviceFromUser(request.getUserId(), request.getDeviceId());
        return ResponseEntity.ok("Device unassigned successfully");
    }

    @Operation(summary = "Obține dispozitivele unui utilizator specific (Admin)", description = "Returnează lista de dispozitive pentru un ID de utilizator specificat. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista dispozitivelor găsită",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu a fost găsit", content = @Content)
    })

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getUserDevices(@PathVariable Long userId,
                                                          @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to get devices for user: {}", userId);
        List<DeviceDTO> devices = userDeviceService.getDevicesByUserId(userId);
        return ResponseEntity.ok(devices);
    }

    @Operation(summary = "Șterge toate asocierile unui utilizator (Intern)", description = "Șterge *toate* asocierile pentru un utilizator. Endpoint intern, apelat de 'user-service' la ștergerea unui utilizator. Ar trebui securizat.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Asocieri șterse", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu a fost găsit", content = @Content)
    })

    @DeleteMapping("/by-user/{userId}")
    public ResponseEntity<Void> deleteAssignmentsForUser(@PathVariable Long userId)
    {
        userDeviceService.deleteAssignmentsByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obține dispozitivele mele (Client)", description = "Returnează lista de dispozitive asociate clientului autentificat. Necesită rol CLIENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista dispozitivelor mele",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Neautorizat (header-ul X-User-Id lipsește)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces refuzat (Rolul nu este CLIENT)", content = @Content)
    })

    @GetMapping("/my-devices")
    public ResponseEntity<List<DeviceDTO>> getMyAssignedDevices(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long myUserId) {

        if (!"CLIENT".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Requires CLIENT role.");
        }

        log.info("CLIENT request to get devices for user: {}", myUserId);

        List<DeviceDTO> deviceDTOs = userDeviceService.getDevicesByUserId(myUserId);

        return ResponseEntity.ok(deviceDTOs);
    }
}