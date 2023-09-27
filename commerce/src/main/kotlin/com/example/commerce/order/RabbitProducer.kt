package com.example.commerce.order

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rabbit")
class RabbitController(private val rabbitProducer: RabbitProducer) {
    @PostMapping("/message")
    fun sendMessage(@RequestBody message : String) {
        rabbitProducer.send(message);
    }
}

// 메시지를 만들어서 보내
// 생산자(producer)
@Service
class RabbitProducer(private val rabbitTemplate: RabbitTemplate) {
    fun send(message: String) {
        rabbitTemplate.convertAndSend("my-queue", message)
    }
}