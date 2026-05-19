package com.whatsyouretf.userservice.domain.portfolio.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.portfolio.service.PresetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Preset", description = "꾸러미 API")
@RestController
@RequestMapping("/api/v1/presets")
@RequiredArgsConstructor
public class PresetController {

    private final PresetService presetService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PresetListResponse>>> getPresetsList() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(PresetListResponse.of(presetService.getPresets())));
    }

    @GetMapping("/{presetId}")
    public ResponseEntity<ApiResponse<PresetDetail>> getPreset(@PathVariable  Long presetId) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(presetService.getPreset(presetId)));
    }
}
