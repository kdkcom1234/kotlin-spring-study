package com.example.sales

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class TopProductResponse(
    val id: Int,
    val name: String,
    val image: String
)

@RestController
@RequestMapping("/products")
class ProductController {

    val topProducts = listOf(
        TopProductResponse(1, "제품1", "http://.../1.png"),
        TopProductResponse(2, "제품2", "http://.../2.png"),
        TopProductResponse(3, "제품3", "http://.../3.png")
    )

    @GetMapping("/top-promotion")
    fun getTopPromotion() : List<TopProductResponse> {
        return topProducts;
    }
}