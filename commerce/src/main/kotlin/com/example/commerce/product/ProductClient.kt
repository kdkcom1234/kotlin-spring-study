package com.example.commerce.product

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

data class TopProductResponse(
    val id: Int,
    val name: String,
    val image: String
)
@FeignClient(name="productClient")
interface ProductClient {
    @GetMapping("/top-promotion")
    fun getTopPromotion() : List<TopProductResponse>
}