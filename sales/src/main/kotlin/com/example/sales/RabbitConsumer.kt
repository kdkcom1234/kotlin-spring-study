package com.example.sales

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

data class Order (
    val id: Long,
    val name : String,
    val address : String,
    val orderItems : List<OrderItem>
)
data class OrderItem (
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity : Int,
    val unitPrice : Long
)

@Service
class RabbitConsumer {
    @RabbitListener(queues = ["my-queue"])
    fun receive(message : String) {
        // auto-ack 모드
        // listner 함수가 정상적으로 수행되면
        // rabbitmq에 ack 신호를 보냄, 메시지가 삭제
        println("Received Message: $message")
    }
}

