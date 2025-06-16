package com.ugur.talk2notes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/audio/")
@Tag(
        name = "User Audio API",
        description = "User APIs for managing audio")
@RequiredArgsConstructor
public class AudioController {
    @GetMapping("")
    @Operation(
            summary = "Get race winners for a specific season",
            description = "Retrieves all race winners from the database for the specified F1 season")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved race winners"),
                    @ApiResponse(responseCode = "404", description = "No data found for the given season"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<List<String>> getAudio() {

        log.info("Received get request for audio}");

        return ResponseEntity.ok(List.of());
    }
}
