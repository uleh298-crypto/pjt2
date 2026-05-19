package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.portfolio.controller.EtfPresetResponse;
import com.whatsyouretf.userservice.domain.portfolio.controller.PresetDetail;
import com.whatsyouretf.userservice.domain.portfolio.entity.PresetPortfolio;
import com.whatsyouretf.userservice.domain.portfolio.service.PresetSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PresetRepositoryImpl implements PresetRepository {

    private final PresetPortfolioJpaRepository presetPortfolioJpaRepository;
    private final PresetPortfolioEtfJpaRepository presetPortfolioEtfJpaRepository;

    @Override
    public List<PresetSummary> findAll() {
        return presetPortfolioJpaRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .flatMap(p -> toTagList(p.getTags()).stream()
                        .map(tag -> new PresetSummary(p.getId(), p.getName(), p.getDescription(), tag, p.getImageTag())))
                .toList();
    }

    @Override
    public PresetDetail findByPresetId(Long presetId) {
        PresetPortfolio preset = presetPortfolioJpaRepository.findById(presetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        List<EtfPresetResponse> etfs = presetPortfolioEtfJpaRepository
                .findAllByPresetPortfolioId(presetId)
                .stream()
                .map(pe -> new EtfPresetResponse(pe.getEtf().getStockCode(), pe.getEtf().getName()))
                .toList();

        return new PresetDetail(preset.getId(), preset.getName(), preset.getDescription(), preset.getImageTag(), etfs);
    }

    private List<String> toTagList(String tags) {
        if (tags == null || tags.isBlank()) return List.of("");
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .toList();
    }
}
