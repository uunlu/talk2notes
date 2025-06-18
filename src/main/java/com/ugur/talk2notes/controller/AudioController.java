package com.ugur.talk2notes.controller;

import com.ugur.talk2notes.dto.AudioUploadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/audio")
public class AudioController {
    @PostMapping
    public ResponseEntity<String> uploadAudio(@RequestBody AudioUploadRequest request) {
        // Implementation
        return ResponseEntity.ok().build();
    }
}
