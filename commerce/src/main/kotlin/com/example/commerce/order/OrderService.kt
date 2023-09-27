package com.example.commerce.order

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// controller - req /  biz-logic  / res
// biz-logic: 연산, 데이터처리, 시스템간 상호작용
// service - biz-logic

@Service
class OrderService(private val rabbitTemplate: RabbitTemplate) {
    private val mapper = jacksonObjectMapper()

    fun createOrder(orderRequest: Order) {
        // 주문정보 저장 트랜잭션
        // ...
        orderRequest.id = 1
        for((index, item) in orderRequest.orderItems.withIndex()) {
            item.id = (index + 1).toLong()
        }

        // sales service로 주문정보 전송
        sendOrder(orderRequest)
    }

    fun sendOrder(orderRequest: Order) {
        rabbitTemplate.convertAndSend("create-order", mapper.writeValueAsString(orderRequest))
    }
}