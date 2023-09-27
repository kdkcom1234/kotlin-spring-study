package com.example.sales

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {

    // SSE(Server Sent Event)
    // Event(이벤트): 데이터 변경이 일어남
    // http 프로토콜로 작동되는 웹 표준방식
    // 응답형식 SseEmitter 반환을 해줘야함
    // 문자열만 보낼 수 있음
    @GetMapping("/notifications")
    fun streamNotification(): SseEmitter {
        return orderService.createEmitter()
    }
}