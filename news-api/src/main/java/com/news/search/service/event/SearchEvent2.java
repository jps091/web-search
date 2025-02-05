package com.news.search.service.event;

import java.time.LocalDateTime;

public record SearchEvent2(String query, LocalDateTime timestamp) {
}
