package com.example.rest.springbatch.util.cache;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class DbCacheService {

    public String findBatchYearCurrentMonth() {
        LocalDate today = LocalDate.now(); // gets the current calendar/system date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        return today.format(formatter);
    }

    public void cacheData() {
        // Logic to cache data from DB
    }

    public void clearCache() {
        // Logic to clear cached data
    }

    public Object getCachedValue(String key) {
        // Logic to fetch value from cache
        return null;
    }
}
