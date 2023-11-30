package com.example.commerce.product

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {
    private val POST_FILE_PATH = "tmp/files/post";

    @GetMapping("/top-promotion")
    fun fetchTopPromotion() : List<TopProductResponse>? {
        return productService.getCachedTopPromotion()
    }

    @PostMapping("/file")
    fun createWithFile(@RequestParam files: Array<MultipartFile>) {

        // files/post
        val dirPath = Paths.get(POST_FILE_PATH)
        if (!Files.exists(dirPath)) {
            // 폴더 생성
            Files.createDirectories(dirPath)
        }

        val filesList = mutableListOf<Map<String, String?>>()

        runBlocking {
            files.forEach {
                launch {
                    println("filename: ${it.originalFilename}")
                    // files/post/1.png
                    // 339993-392039-a9d9d9-d9d9d9d.png
                    // uuid: 랜덤하게 문자열로 id값을 만들 수 있는 방법(순서X)
                    val uuidFileName =
                        // 파일명
                        buildString {
                            append(UUID.randomUUID().toString())
                            append(".") // 확장자
                            append(it.originalFilename!!.split(".").last())
                        }
                    val filePath = dirPath.resolve(uuidFileName)
                    // 파일객체에서 스트림객체를 얻어옴
                    // 스트림객체: 바이트배열 처리할 수 있는 객체
                    it.inputStream.use {
                        // 파일 저장
                        Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
                    }

                    // 파일의 메타데이터를 리스트-맵에 임시저장
                    filesList.add(mapOf("uuidFileName" to uuidFileName,
                        "contentType" to it.contentType,
                        "originalFileName" to it.originalFilename))
                }
            }
        }
    }
}