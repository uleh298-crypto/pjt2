package com.whatsyouretf.userservice.domain.user.service;

import com.whatsyouretf.userservice.domain.user.service.impl.MyDataEtfCount;

import java.util.List;

public interface MyDataApi {
    List<MyDataEtfCount> getMyData(Long userId);
}
