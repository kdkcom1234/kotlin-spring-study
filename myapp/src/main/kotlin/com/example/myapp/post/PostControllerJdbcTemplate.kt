package com.example.myapp.post

import com.example.myapp.auth.AuthProfile
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("posts/jdbc")
class PostControllerJdbcTemplate(
    private val template: JdbcTemplate,
    private val namedTemplate : NamedParameterJdbcTemplate
) {
//    @GetMapping
//    fun fetch() = transaction {
//        Posts.selectAll().map { r -> PostResponse(
//            r[Posts.id], r[Posts.title], r[Posts.content],
//            r[Posts.createdDate].toString())
//        }
//    }

    // JAVA 버전
    /*
    *     @GetMapping
        public List<PostResponse> fetch() {
            String sql = "SELECT * FROM post";
            return template.query(sql, new RowMapper<PostResponse>() {
                @Override
                public PostResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new PostResponse(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDate("created_date").toString()
                    );
                }
            });
        }
    * */

    @GetMapping
    fun fetch(): MutableList<PostResponse> =
        // template.query(sql) { resultset mapper }
        template.query("SELECT * FROM post") {
          // rs: ResulSet
          rs, _ -> PostResponse(
              rs.getLong("id"),
              rs.getString("title"),
              rs.getString("content"),
              rs.getString("created_date")
      )
    }

    @GetMapping("/paging/search")
    fun searchPaging(
        @RequestParam size : Int,
        @RequestParam page : Int,
        @RequestParam keyword : String?) : Page<PostResponse> {
        // sql인젝션 유도 매개변수
//        val keyword2 = "자바%' OR 1=1 OR '"
        val countFrom = "SELECT count(*) FROM post"
        
        // :매개변수명
        // name-parameter 방식
        
        // ? ? ?
        // sequential-parameter 방식
        // [title, content, id]
        
        val where = when {
            keyword != null -> "WHERE title LIKE :keyword OR content LIKE :keyword"
            else -> ""
        }

        println("$countFrom $where")

        // count 쿼리 실행
        // query -> List, querForObject -> Object
        // namedTemplate.queryForObject(sql, argsMap) { resultset mapper }

        val totalCount = namedTemplate.queryForObject(

            // SQL 문
            "$countFrom $where",

            // SQL 매개변수
            // {"keyword": "%검색어%"}
            mapOf("keyword" to "%$keyword%")) {

                // RowMapper
                // 결과집합의 첫번째열값을 Long으로 변환하여 반환
                rs, _ -> rs.getLong(1)
        }

        // 조건에 맞는 레코드가 없을 때 반환
        if(totalCount == null || totalCount == 0L) {
            return PageImpl(
                listOf<PostResponse>(),
                PageRequest.of(page, size),
                0)
        }

        val selectFrom = "SELECT * FROM post"
        val orderByLimitOffset = """
            ORDER BY id DESC
            LIMIT $size OFFSET ${size * page}
            """.trimIndent()

        println("$selectFrom $where $orderByLimitOffset")

        // select 쿼리 실행
        val content = namedTemplate.query(
            // SQL문
            "$selectFrom $where $orderByLimitOffset",
            // 매개변수 맵
            mapOf("keyword" to "%$keyword%") ) {
            // RowMapper
            rs, _ ->
            PostResponse(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("created_date")
            )
        }

        // 페이지 객체 반환
        return PageImpl(content, PageRequest.of(page, size),  totalCount)
    }

    @GetMapping("/commentCount")
    fun fetchCommentCount(@RequestParam size : Int, @RequestParam page : Int,
                          @RequestParam keyword : String?) : Page<PostCommentCountResponse> {

        var countFrom = "SELECT count(*) FROM post"
        var where = when {
            keyword != null -> "WHERE title LIKE :keyword OR content LIKE :keyword"
            else -> ""
        }

        // count 쿼리 실행
        val totalCount = namedTemplate.queryForObject(
            "$countFrom $where",
            mapOf("keyword" to "%$keyword%")) { rs, _ ->
            rs.getLong(1)
        }

        // 조건에 맞는 레코드가 없을 때 반환
        if (totalCount == null || totalCount == 0L) {
            return PageImpl(listOf<PostCommentCountResponse>(), PageRequest.of(page, size), 0)
        }

        val selectCountFrom = """
            SELECT p.id, p.title, p.created_date, p.profile_Id, pf.nickname,
                count(c.id) as commentCount
            FROM post p
                INNER JOIN profile pf on p.profile_id = pf.id
                LEFT join post_comment c on p.id = c.post_id
        """.trimIndent()

        val groupByOrderByLimitOffset = """
            GROUP BY p.id, p.title, p.created_date, p.profile_Id, pf.nickname
            ORDER BY id DESC
            LIMIT $size OFFSET ${size * page}
        """.trimIndent()

        println("$selectCountFrom $where $groupByOrderByLimitOffset")

        // select 쿼리 실행
        val content = namedTemplate.query(
            "$selectCountFrom $where $groupByOrderByLimitOffset",
            mapOf("keyword" to "%$keyword%")) {
            rs, _ -> PostCommentCountResponse(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("created_date"),
                rs.getLong("profile_id"),
                rs.getString("nickname").toString(),
                rs.getLong("commentCount")
            )
        }

        // 페이지 객체 반환
        return PageImpl(content, PageRequest.of(page, size),  totalCount)
    }

    @PostMapping
    fun create(@RequestBody request : PostCreateRequest,
               @RequestAttribute authProfile: AuthProfile?)
    : ResponseEntity<Map<String, Any?>> {
        // 요청값 검증
        if(!request.validate()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "title and content fields are required"))
        }

        // 저장할 컬럼 값
        val (title, content) = request
        val dateTime = LocalDateTime.now();

        // insert 실행
        // template.insert(X)

        // template.update("insert 구문") : Int(영향받은 행수)
        // insert 개수, update한 행(record) 개수, delete한 행 개수
        // 못쓰는 상황

        // auto-increment로 생성된 id값을 조회
        // template.query("SELECT last_insert_id()")
        // MySQL 전용

        val insertedId = SimpleJdbcInsert(template)
            // INSERT INTO post
            .withTableName("post")
            .usingGeneratedKeyColumns("id")
            // auto-increment 필드 제외 모두 넣는 경우에는 안 해도 됨
            // INSERT INTO post(title, content, created_date, profile_id)
            .usingColumns(
                "title",
                "content",
                "created_date",
                "profile_id"
            )
            // VALUE (.....)
            .executeAndReturnKey(
                mapOf("title" to title,
                    "content" to content,
                    "created_date" to dateTime,
                    "profile_id" to 1)) // authProfile.id

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapOf("data" to 
                    PostResponse(
                        // Long 또는 Int로 변환
                        insertedId.toLong(), 
                        title, content, 
                        dateTime.toString())))

    }

    @DeleteMapping("/{id}")
    fun remove(@PathVariable id : Long,
               @RequestAttribute authProfile: AuthProfile?
    ) : ResponseEntity<Any> {
        // id와 profile_id로 조회, 없으면 NOT_FOUND
        val result = template.query(
            // SQL
            "SELECT id FROM post WHERE id = ? AND profile_id = ?",
            // Parameters(Object... args)
            // post.id, authProfile.id
            arrayOf(id, 1) ) { rs, _ -> rs }

        // query
        // 조건에 맞는 레코드가 없어도 빈 리스트를 반환
        // queryForObject
        // 조건 맞는 레코드가 없으면 예외 발생

//        val result = template.query(
//            // SQL
//            "SELECT id FROM post WHERE id = ? AND profile_id = ?",
//            // RowMapper
//            { rs, _ -> rs },
//            // Parameters(Object... args)
//            // post.id, authProfile.id
//            arrayOf(id, 1))

        if(result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        // delete 실행
        // 영향받은 레코드 수가 반환 된다.
        // insert, update, delete 구문은 template.update("...")
        val affected = template.update("DELETE FROM post WHERE id = ?", id)
        println(affected) // 1 (1건이 영향을 받음)

        // 200 OK
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{id}")
    fun modify(@PathVariable id : Long,
               @RequestBody request: PostModifyRequest,
               @RequestAttribute authProfile: AuthProfile?
    ): ResponseEntity<Any> {
        val (title, content) = request
        // 둘다 널이거나 빈값이면 400 : Bad request
        if(title.isNullOrEmpty() && content.isNullOrEmpty()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "title or content are required"))
        }

        // id에 해당 레코드가 없으면 404
        val result = template.query(
            // SQL
            "SELECT id FROM post WHERE id = ? AND profile_id = ?",
            // RowMapper
            { rs, _ -> rs },
            // Parameters(Object... args)
            // post.id, authProfile.id
            id, 1)

        if(result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        // update 필드구문 생성
        val setFields = when {
            content.isNullOrEmpty() -> "SET title = :title" // 타이틀만
            title.isNullOrEmpty() -> "SET content = :content" // 컨텐트만
            else -> "SET title = :title, content = :content" // 둘다
        }
        // 매개변수 객체 생성
        val args = mutableMapOf<String, Any>("id" to id);
        if(!title.isNullOrEmpty()) {
            args["title"] = title
        }
        if(!content.isNullOrEmpty()) {
            args["content"] = content
        }

        val updateStatement = "UPDATE post $setFields WHERE id = :id "
        // UPDATE 실행
        // 영향받은 레코드 수가 반환 된다.
        val affected = namedTemplate.update(updateStatement, args)
        println(affected)

        return ResponseEntity.ok().build();

        // 트랜잭션 처리
        // @Service class
        // @Transactional fun .. (...) {   }
        // 함수에 예외처리가 생기면 rollback
        // 함수 안에 SQL외에 예외처리가 발생할 만한 다른 것을 넣지 않음
    }
}