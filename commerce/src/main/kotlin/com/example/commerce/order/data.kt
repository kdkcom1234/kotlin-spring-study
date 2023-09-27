package com.example.commerce.order

data class Order (
    var id: Long,
    val name : String,
    val address : String,
    val orderItems : List<OrderItem>
)

data class OrderItem (
    var id: Long,
    val productId: Long,
    val productName: String,
    val quantity : Int,
    val unitPrice : Long
)
