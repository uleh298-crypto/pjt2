package com.whatsyouretf.userservice.domain.user.service.impl;

import com.whatsyouretf.userservice.domain.user.service.MyDataApi;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MockMyDataApiImpl implements MyDataApi {
    @Override
    public List<MyDataEtfCount> getMyData(Long userId) {
        return List.of(
            new MyDataEtfCount("069500", new BigDecimal(10)),
            new MyDataEtfCount("091160", new BigDecimal(15)),
            new MyDataEtfCount("102780", new BigDecimal(20))
        );
    }
}
