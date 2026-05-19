package com.whatsyouretf.userservice.domain.portfolio.controller;

import java.util.List;

public record PresetDetail(
    Long presetId,
    String presetName,
    String description,
    String imageTag,
    List<EtfPresetResponse> presetResponseList
) {
}
