package com.example.sales

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@Service
class OrderService {
    private val mapper = jacksonObjectMapper()
    // emit: 발생시키다
    // emitter: 발생시키는객체
    // SseEmitter: 서버에서 보낸 이벤트를 발생시키는 객체
    // SseEmitter는 접속된 클라이언트마다 생성이 되어야함
    // array, list 같은 컬렉션으로 목록관리

    private val emitters = mutableListOf<SseEmitter>()

    @RabbitListener(queues = ["create-order"])
    fun receiveOrder(message: String) {
        val order : Order = mapper.readValue(message)
        println("Received Order: $order")

        val deadEmitters: MutableList<SseEmitter> = ArrayList()

        // 전체 emitter 목록을 탐색해서 전체 전송
        for (emitter in emitters) {
            try {
                emitter.send(message)
            } catch (e: IOException) {
                deadEmitters.add(emitter)
            }
        }
        // send 불가능한 객체들 삭제
        emitters.removeAll(deadEmitters)
    }

    fun createEmitter(): SseEmitter {
        // 클라이언트에서 응답받을 수 있는 객체를 생성하고 리스트 추가
        val emitter = SseEmitter()
        emitters.add(emitter)

        // 클라이언트의 접속에 제한시간 지나면
        emitter.onTimeout {
            emitters.remove(emitter)
        }
        // 접속이 끊기거나 만료됐을 때
        emitter.onCompletion {
            emitters.remove(emitter)
        }
        // 기본 메시지 전송
        // 기본 메시지를 전송 안하면 pending처리 됨
        emitter.send("connected");

        return emitter
    }
}