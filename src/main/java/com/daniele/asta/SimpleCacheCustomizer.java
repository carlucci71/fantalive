package com.daniele.asta;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

@Component
public class SimpleCacheCustomizer 
  implements CacheManagerCustomizer<ConcurrentMapCacheManager> {
 
    @Override
    public void customize(ConcurrentMapCacheManager cacheManager) {
        cacheManager.setCacheNames(Arrays.asList("allenatori"));
    }
}