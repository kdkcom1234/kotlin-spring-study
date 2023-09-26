package com.example.commerce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
class CommerceApplication

fun main(args: Array<String>) {
	runApplication<CommerceApplication>(*args)
}





