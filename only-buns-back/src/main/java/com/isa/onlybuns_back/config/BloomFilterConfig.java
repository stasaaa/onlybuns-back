package com.isa.onlybuns_back.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

    private static final int EXPECTED_INSERTIONS = 1000000;
    private static final double FPP = 0.01;

    @Bean
    public BloomFilter<String> usernameBloomFilter() {
        return BloomFilter.create(Funnels.stringFunnel(java.nio.charset.StandardCharsets.UTF_8), EXPECTED_INSERTIONS, FPP);
    }
}