package com.whatsyouretf.userservice.domain.company.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AssetManager {
        KODEX("삼성자산운용"),
        TIGER("미래에셋자산운용");
        private final String companyName;

        public static String getCompanyNameByCode(String code) {
                return AssetManager.valueOf(code).getCompanyName();
        }
}
