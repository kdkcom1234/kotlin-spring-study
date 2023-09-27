package com.example.commerce.product

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProductService(private val productClient: ProductClient,
    private val redisTemplate: RedisTemplate<String, String>) {
    // Object <-> JSON
    private val mapper = jacksonObjectMapper()

    // 애플리케이션을 시작하면 한 번은 수행됨
//    @Scheduled(fixedRate = 1000 * 60 * 60)
//    @Scheduled(cron = "10 * * * * * *")
    fun scheduledFetchTopPromotion() {
        println("--called by schedule: ${Date().time}--")
        val result = productClient.getTopPromotion()
        println(result)
        // RedisTemplate<key=String, value=String>
        // default: localhost:6379
        redisTemplate.delete("top-promotion") // 캐시 데이터 삭제
        // 캐시 데이터 생성
        redisTemplate.opsForValue()
            .set("top-promotion", mapper.writeValueAsString(result))
    }

    fun getCachedTopPromotion() : List<TopProductResponse> {
        val result = redisTemplate.opsForValue().get("top-promotion")
        if(result != null) {
            // value(json) -> List<TopProductResponse>
            val list : List<TopProductResponse> = mapper.readValue(result)
            return list
        } else {
            // 빈배열 반환
            return listOf()
        }
    }
}