package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.portfolio.controller.PresetDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresetServiceImpl implements PresetService {
    private final PresetReader presetReader;
    @Override
    @Transactional(readOnly = true)
    public List<PresetSummary> getPresets() {
        return presetReader.getPresetList();
    }

    @Override
    public PresetDetail getPreset(Long presetId) {
        return presetReader.getPresetDetail(presetId);
    }
}
