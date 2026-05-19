package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.portfolio.controller.PresetDetail;

import java.util.List;

public interface PresetReader {
    List<PresetSummary> getPresetList();

    PresetDetail getPresetDetail(Long presetId);
}
