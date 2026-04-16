package com.game.dataservice.service;

import com.game.dataservice.repository.RegionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionCacheService {

    private final RegionRepository regionRepository;
    private final Map<Integer, String> regionMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Loading regions into memory cache...");
        regionRepository.findAll().forEach(r -> regionMap.put(r.getId(), r.getName()));
        log.info("Loaded {} regions.", regionMap.size());
    }

    public String getName(Integer id) {
        if (id == null) return "未知";
        return regionMap.getOrDefault(id, "未知");
    }
}
