package com.news.search.service.event;

import com.news.searchinfo.infrastructure.SearchInfoJdbcCommandRepository;
import com.news.searchinfo.infrastructure.SearchInfoJdbcQueryRepository;
import com.news.searchinfo.model.SearchInfo;
import com.news.search.service.response.SearchInfoQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEventHandler {

    private final SearchInfoJdbcQueryRepository queryRepository;
    private final SearchInfoJdbcCommandRepository commandRepository;

    @Async("event-Executor1")
    @EventListener
    public void handleEvent1(SearchEvent event){
        log.info("[Queue-SIZE-0 SearchEventHandler] handleEvent: {}", event);
        List<String> inputQueryList = parseInputQueryList(event.query());
        List<SearchInfoQueryResponse> savedQueryList = queryRepository.findByQueryList(inputQueryList);


        if(isExistInputSearchInfo(savedQueryList)){
            List<SearchInfo> searchInfoList = extractNewSearchQueryList(inputQueryList, savedQueryList, event.timestamp());
            commandRepository.saveAll(searchInfoList);

            List<Integer> ids = getSavedSearchInfoIds(savedQueryList);
            commandRepository.increaseSearchCount(ids);
            return;
        }

        List<SearchInfo> inputSearchInfoList = SearchInfo.create(inputQueryList, event.timestamp());
        commandRepository.saveAll(inputSearchInfoList);
    }

    @Async("event-Executor2")
    @EventListener
    public void handleEvent2(SearchEvent2 event){
        log.info("[Queue-SIZE-5 SearchEventHandler] handleEvent: {}", event);
        List<String> inputQueryList = parseInputQueryList(event.query());
        List<SearchInfoQueryResponse> savedQueryList = queryRepository.findByQueryList(inputQueryList);


        if(isExistInputSearchInfo(savedQueryList)){
            List<SearchInfo> searchInfoList = extractNewSearchQueryList(inputQueryList, savedQueryList, event.timestamp());
            commandRepository.saveAll(searchInfoList);

            List<Integer> ids = getSavedSearchInfoIds(savedQueryList);
            commandRepository.increaseSearchCount(ids);
            return;
        }

        List<SearchInfo> inputSearchInfoList = SearchInfo.create(inputQueryList, event.timestamp());
        commandRepository.saveAll(inputSearchInfoList);
    }

    private static boolean isExistInputSearchInfo(List<SearchInfoQueryResponse> savedQueryList) {
        return !savedQueryList.isEmpty();
    }

    private List<SearchInfo> extractNewSearchQueryList(List<String> inputQueryList, List<SearchInfoQueryResponse> savedQueryList, LocalDateTime timeStamp){
        List<String> savedResults = getSavedSearchInfoQueries(savedQueryList);
        List<String> newQueryList = getDifference(inputQueryList, savedResults);
        return SearchInfo.create(newQueryList, timeStamp);
    }

    private static List<String> getSavedSearchInfoQueries(List<SearchInfoQueryResponse> queryList) {
        return queryList.stream().map(SearchInfoQueryResponse::query).toList();
    }

    private static List<Integer> getSavedSearchInfoIds(List<SearchInfoQueryResponse> queryList) {
        return queryList.stream().map(SearchInfoQueryResponse::id).toList();
    }

    private List<String> getDifference(List<String> source, List<String> target) {


        // 문자열을 공백 기준으로 분리
        Set<String> targetWords = new HashSet<>(target);

        // target에 없는 단어만 필터링
        return source.stream()
                .filter(word -> !targetWords.contains(word))
                .toList();
    }

    // 문자열 파싱 메서드
    private List<String> parseInputQueryList(String input) {
        // 입력 문자열을 공백 기준으로 분리
        String[] words = input.split(" ");
        List<String> result = new ArrayList<>();

        for (String word : words) {
            // 숫자로만 이루어진 단어를 필터링
            if (isNumeric(word)) {
                continue;
            }

            // 영어와 한글 문자만 필터링
            String filteredWord = word.replaceAll("[^a-zA-Z가-힣0-9]", "");

            // 필터링된 결과가 비어 있지 않으면 추가
            if (!filteredWord.isEmpty()) {
                // 영어는 소문자로 변환
                result.add(filteredWord.toLowerCase());
            }
        }

        return result;
    }

    // 문자열이 숫자로만 이루어져 있는지 확인하는 유틸리티 메서드
    private boolean isNumeric(String str) {
        return str.matches("\\d+"); // 문자열이 숫자(0-9)로만 이루어져 있으면 true
    }
}
