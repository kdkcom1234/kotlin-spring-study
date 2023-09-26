package com.example.commerce.product

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

data class TopProductResponse(
    val id: Int,
    val name: String,
    val image: String
)
@FeignClient(name="productClient", url="http://192.168.100.62:8082/products")
interface ProductClient {
    @GetMapping("/top-promotion")
    fun getTopPromotion() : List<TopProductResponse>
}