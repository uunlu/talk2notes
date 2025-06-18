package com.ugur.talk2notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class AudioUploadRequest {
    @NotNull(message = "Audio file is required")
    private MultipartFile audioFile;

    @NotBlank(message = "Title is required")
    private String title;
}
