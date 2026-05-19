package com.whatsyouretf.userservice.domain.portfolio.service;

public record PresetSummary(
    Long presetId,
    String name,
    String description,
    String tag,
    String imageTag
) {

}
