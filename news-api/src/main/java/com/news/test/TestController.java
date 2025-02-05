package com.news.test;

import com.news.search.controller.request.SearchRequest;
import com.news.search.controller.response.PageResult;
import com.news.search.controller.response.SearchResponse;
import com.news.search.controller.response.StatResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/test")
public class TestController {
    private final TestService testService;

    @GetMapping("/no-event")
    public PageResult<SearchResponse> searchNoEvent(){
        String query = "Initialized JPA EntityManagerFactory for persistence unit default";
        int page = 1;
        int size = 15;
        return testService.searchNoEvent(query, page, size);
    }

    @GetMapping("/event")
    @RateLimiter(name = "apiRateLimiter")
    public PageResult<SearchResponse> searchWithEventQueSize0(){
        String query = "Initialized JPA EntityManagerFactory for persistence unit default";
        int page = 1;
        int size = 15;
        return testService.searchWithEvent(query, page, size);
    }

    @GetMapping("/event10")
    @RateLimiter(name = "apiRateLimiter")
    public PageResult<SearchResponse> searchWithEventQueSize10(){
        String query = "Initialized JPA EntityManagerFactory for persistence unit default";
        int page = 1;
        int size = 15;
        return testService.searchWithEvent2(query, page, size);
    }

    @GetMapping("/jpa/stats")
    public StatResponse findQueryStatsByJpa(@RequestParam(name = "query") String query,
                                       @RequestParam(name = "date", required = false)
                                       LocalDate date) {
        LocalDate localDate = (date != null) ? date : LocalDate.now();
        return testService.findQueryCountByJpa(query, localDate);
    }

    @PostMapping
    public ResponseEntity<String> create(){
        String query = "Initialized JPA EntityManagerFactory for persistence unit default";
        testService.create(query);
        return ResponseEntity.status(HttpStatus.OK).body("벌크 쿼리 실행 성공");
    }

    @PostMapping("/jpa")
    public ResponseEntity<String> createByJpa(){
        String query = "Initialized JPA EntityManagerFactory for persistence unit default";
        testService.createByJpa(query);
        return ResponseEntity.status(HttpStatus.OK).body("JPA 쿼리 실행 성공");
    }

    @PostMapping("/search")
    public ResponseEntity<String> createBySearch(){
        String query = "Initialized JPA EntityManagerFactory for persistence unit default";
        testService.handleEvent2(query);
        return ResponseEntity.status(HttpStatus.OK).body("Search Info 쿼리 실행 성공");
    }

    @GetMapping("/group/stats/ranking")
    public List<StatResponse> findTop5StatsByJpa() {
        return testService.findTop5QueryGroupBy();
    }

    @GetMapping("/count/stats/ranking")
    public List<StatResponse> findTop5StatsByJdbc() {
        return testService.findTop5QueryByCount();
    }

    @PostMapping("/total/count")
    public List<StatResponse> totalCount(){
        String query = "Initialized JPA EntityManagerFactory for persistence unit default";
        return testService.totalCount(query);
    }

    @PostMapping("/total/group")
    public List<StatResponse> totalGroupBy(){
        String query = "Initialized JPA EntityManagerFactory for persistence unit default";
        return testService.totalGroupBy(query);
    }
}
