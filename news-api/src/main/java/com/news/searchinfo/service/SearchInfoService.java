package com.news.searchinfo.service;

import com.news.searchinfo.infrastructure.SearchInfoJdbcCommandRepository;
import com.news.searchinfo.model.SearchInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchInfoService {

    private final SearchInfoJdbcCommandRepository commandRepository;

    public void increaseAndSaveAll(List<SearchInfo> searchInfoList, List<Integer> ids){
        commandRepository.saveAll(searchInfoList);
        commandRepository.increaseSearchCount(ids);
    }

    public void saveAll(List<SearchInfo> searchInfoList){
        commandRepository.saveAll(searchInfoList);
    }
}
