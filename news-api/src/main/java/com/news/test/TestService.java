package com.news.test;

import com.news.dailystat.model.DailyStat;
import com.news.dailystat.service.DailyStatCommandService;
import com.news.dailystat.service.DailyStatQueryService;
import com.news.dailystat.service.response.DailyStatQueryResponse;
import com.news.search.controller.response.PageResult;
import com.news.search.controller.response.SearchResponse;
import com.news.search.controller.response.StatResponse;
import com.news.search.service.WebQueryService;
import com.news.search.service.event.SearchEvent;
import com.news.search.service.event.SearchEvent2;
import com.news.searchinfo.model.SearchInfo;
import com.news.search.service.response.PageQueryResult;
import com.news.search.service.response.SearchInfoQueryResponse;
import com.news.search.service.response.SearchQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class TestService {
    private final WebQueryService webQueryService;
    private final DailyStatCommandService dailyStatCommandService;
    private final DailyStatQueryService dailyStatQueryService;
    private final TestSearchInfoJdbcRepository testSearchInfoJdbcRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**아래는 테스트용 메서드*/

    public PageResult<SearchResponse> searchNoEvent(String query, int page, int size){

        List<String> eventQueryList = parseString(query);

        List<SearchInfoQueryResponse> queryList = testSearchInfoJdbcRepository.findByQueryList(eventQueryList);
        List<Integer> ids = queryList.stream().map(SearchInfoQueryResponse::id).toList();
        if(!ids.isEmpty()){
            testSearchInfoJdbcRepository.incrementSearchCount(ids);
        }

        List<String> savedQueryList = queryList.stream().map(SearchInfoQueryResponse::query).toList();

        List<String> newQueryList = getDifference(eventQueryList, savedQueryList);
        List<SearchInfo> searchInfoList = SearchInfo.create(newQueryList, LocalDateTime.now());

        testSearchInfoJdbcRepository.saveAll(searchInfoList);

        PageQueryResult<SearchQueryResponse> pageQueryResponse = webQueryService.search(query, page, size);
        return convertToPageResult(pageQueryResponse);
    }

    public PageResult<SearchResponse> searchWithEvent(String query, int page, int size){
        PageQueryResult<SearchQueryResponse> pageQueryResponse = webQueryService.search(query, page, size);
        if(isNotEmptyQueryResponse(pageQueryResponse)){
            log.info("검색결과 개수: {}", pageQueryResponse.size());
            eventPublisher.publishEvent(new SearchEvent(query, LocalDateTime.now()));
        }
        DailyStat dailyStat = DailyStat.create("test", LocalDateTime.now()); // TODO 삭제
        dailyStatCommandService.save(dailyStat);
        return convertToPageResult(pageQueryResponse);
    }

    public PageResult<SearchResponse> searchWithEvent2(String query, int page, int size){
        PageQueryResult<SearchQueryResponse> pageQueryResponse = webQueryService.search(query, page, size);
        if(isNotEmptyQueryResponse(pageQueryResponse)){
            log.info("검색결과 개수: {}", pageQueryResponse.size());
            eventPublisher.publishEvent(new SearchEvent2(query, LocalDateTime.now()));
        }
        DailyStat dailyStat = DailyStat.create("test", LocalDateTime.now()); // TODO 삭제
        dailyStatCommandService.save(dailyStat);
        return convertToPageResult(pageQueryResponse);
    }

    @Transactional
    public List<StatResponse> totalCount(String query){
        log.info("[SearchEventHandler] handleEvent: {}", query);
        List<String> eventQueryList = parseString(query);

        List<SearchInfoQueryResponse> queryList = testSearchInfoJdbcRepository.findByQueryList(eventQueryList);
        List<Integer> ids = queryList.stream().map(SearchInfoQueryResponse::id).toList();
        if(!ids.isEmpty()){
            testSearchInfoJdbcRepository.incrementSearchCount(ids);
        }

        List<String> savedQueryList = queryList.stream().map(SearchInfoQueryResponse::query).toList();

        List<String> newQueryList = getDifference(eventQueryList, savedQueryList);
        List<SearchInfo> searchInfoList = SearchInfo.create(newQueryList, LocalDateTime.now());

        testSearchInfoJdbcRepository.saveAll(searchInfoList);

        List<DailyStatQueryResponse> queryResponse = testSearchInfoJdbcRepository.findTopQuery(15);
        return queryResponse.stream()
                .map(this::toStatResponse)
                .toList();
    }


    public List<StatResponse> totalGroupBy(String query){
        List<String> queryList = parseString(query);
        List<DailyStat> dailyStatList = DailyStat.create(queryList, LocalDateTime.now());
        dailyStatCommandService.saveAll(dailyStatList);

        List<DailyStatQueryResponse> queryResponse = dailyStatQueryService.findTop15Query();
        return queryResponse.stream()
                .map(this::toStatResponse)
                .toList();
    }

    @Transactional
    public void handleEvent2(String query){
        log.info("[SearchEventHandler] handleEvent: {}", query);
        List<String> eventQueryList = parseString(query);

        List<SearchInfoQueryResponse> queryList = testSearchInfoJdbcRepository.findByQueryList(eventQueryList);
        List<Integer> ids = queryList.stream().map(SearchInfoQueryResponse::id).toList();
        if(!ids.isEmpty()){
            testSearchInfoJdbcRepository.incrementSearchCount(ids);
        }

        List<String> savedQueryList = queryList.stream().map(SearchInfoQueryResponse::query).toList();

        List<String> newQueryList = getDifference(eventQueryList, savedQueryList);
        List<SearchInfo> searchInfoList = SearchInfo.create(newQueryList, LocalDateTime.now());

        testSearchInfoJdbcRepository.saveAll(searchInfoList);
    }

    public List<StatResponse> findTop5QueryGroupBy() {
        List<DailyStatQueryResponse> queryResponse = dailyStatQueryService.findTop15Query();
        return queryResponse.stream()
                .map(this::toStatResponse)
                .toList();
    }

    public List<StatResponse> findTop5QueryByCount() {
        List<DailyStatQueryResponse> queryResponse = testSearchInfoJdbcRepository.findTopQuery(15);
        return queryResponse.stream()
                .map(this::toStatResponse)
                .toList();
    }

    public void create(String query){
        List<String> queryList = parseString(query);
        List<DailyStat> dailyStatList = DailyStat.create(queryList, LocalDateTime.now());
        dailyStatCommandService.saveAll(dailyStatList);
    }

    public void createByJpa(String query){
        List<String> queryList = parseString(query);
        List<DailyStat> dailyStatList = DailyStat.create(queryList, LocalDateTime.now());
        dailyStatCommandService.saveAllByJpa(dailyStatList);
    }

    public StatResponse findQueryCountByJpa(String query, LocalDate date) {
        DailyStatQueryResponse queryResponse = dailyStatQueryService.findQueryCountByJpa(query, date);
        return new StatResponse(queryResponse.query(), queryResponse.count());
    }

    private PageResult<SearchResponse> convertToPageResult(PageQueryResult<SearchQueryResponse> pageQueryResponse) {
        List<SearchResponse> searchResponses = pageQueryResponse.contents().stream()
                .map(this::toSearchResponse)
                .toList();
        return new PageResult<>(pageQueryResponse.page(), pageQueryResponse.size(), pageQueryResponse.totalElements(), searchResponses);
    }

    private SearchResponse toSearchResponse(SearchQueryResponse queryResponse){
        return new SearchResponse(queryResponse.title(), queryResponse.link(), queryResponse.description());
    }

    private StatResponse toStatResponse(DailyStatQueryResponse queryResponse){
        return new StatResponse(queryResponse.query(), queryResponse.count());
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
    private List<String> parseString(String input) {
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

    private static boolean isNotEmptyQueryResponse(PageQueryResult<SearchQueryResponse> pageQueryResponse) {
        return !pageQueryResponse.contents().isEmpty();
    }
}
