package com.example.commerce.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {
    @GetMapping("/top-promotion")
    fun fetchTopPromotion() : List<TopProductResponse>? {
        return productService.getCachedTopPromotion()
    }
}