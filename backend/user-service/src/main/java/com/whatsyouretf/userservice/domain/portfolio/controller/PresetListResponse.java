package com.whatsyouretf.userservice.domain.portfolio.controller;

import com.whatsyouretf.userservice.domain.portfolio.service.PresetSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record PresetListResponse(
    Long presetId,
    String title,
    String description,
    String imageTag,
    List<String> presetTagList
) {
    public static List<PresetListResponse> of(List<PresetSummary> presets) {
        Map<Long, PresetListResponse> map = new LinkedHashMap<>();

        for (PresetSummary row : presets) {
            PresetListResponse response = map.computeIfAbsent(
                row.presetId(),
                id -> new PresetListResponse(id, row.name(), row.description(), row.imageTag(), new ArrayList<>())
            );

            response.presetTagList.add(row.tag());
        }

        return new ArrayList<>(map.values());
    }
}
