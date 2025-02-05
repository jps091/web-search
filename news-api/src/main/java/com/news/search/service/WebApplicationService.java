package com.news.search.service;

import com.news.dailystat.service.response.DailyStatQueryResponse;
import com.news.search.controller.response.StatResponse;
import com.news.search.controller.response.PageResult;
import com.news.search.controller.response.SearchResponse;
import com.news.search.service.event.SearchEvent;
import com.news.search.service.response.PageQueryResult;
import com.news.search.service.response.SearchQueryResponse;
import com.news.searchinfo.infrastructure.SearchInfoJdbcQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebApplicationService {
    public static final int QUERY_SIZE = 15;
    private final WebQueryService webQueryService;
    private final SearchInfoJdbcQueryRepository searchInfoJdbcQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PageResult<SearchResponse> search(String query, int page, int size){
        PageQueryResult<SearchQueryResponse> pageQueryResponse = webQueryService.search(query, page, size);
        if(isNotEmptyQueryResponse(pageQueryResponse)){
            log.info("검색결과 개수: {}", pageQueryResponse.size());
            eventPublisher.publishEvent(new SearchEvent(query, LocalDateTime.now()));
        }
        return convertToPageResult(pageQueryResponse);
    }

    @Transactional(readOnly = true)
    public List<StatResponse> findTopQuery() {
        List<DailyStatQueryResponse> queryResponse = searchInfoJdbcQueryRepository.findTopQuery(QUERY_SIZE);
        return queryResponse.stream()
                .map(this::toStatResponse)
                .toList();
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

    private static boolean isNotEmptyQueryResponse(PageQueryResult<SearchQueryResponse> pageQueryResponse) {
        return !pageQueryResponse.contents().isEmpty();
    }
}
