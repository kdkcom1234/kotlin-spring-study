package com.example.sales

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// 백엔드 기능 단위를(도메인)
// 프론트엔드 기능 단위로(feature)
// 인벤토리(재고관리)
@RestController
@RequestMapping("/inventories")
class InventoryController {

    // key: 제품id
    // value: 재고개수
    val stocks = mapOf(1 to 10, 2 to 100, 3 to 5);

    // GET /1 -> 10
    @GetMapping("{productId}")
    fun getProductStocks(@PathVariable productId : Int) : Int? {
        return stocks[productId]
    }
}