package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.portfolio.controller.PresetDetail;

import java.util.List;

public interface PresetService {
    List<PresetSummary> getPresets();

    PresetDetail getPreset(Long presetId);
}
