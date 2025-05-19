    package com.belajar.api.kotlin.repository

    import com.belajar.api.kotlin.model.Category
    import org.springframework.data.jpa.repository.JpaRepository
    import org.springframework.data.jpa.repository.Modifying
    import org.springframework.data.jpa.repository.Query
    import org.springframework.data.repository.query.Param
    import org.springframework.stereotype.Repository

    @Repository
    interface CategoryRepository : JpaRepository<Category, String> {
        @Modifying
        @Query("update Category c set c.deleted = true where c.id = :id")
        fun softDelete(@Param("id") id: String)

        @Query(
            value = "SELECT COUNT(*) > 0 FROM category WHERE name = :name",
            nativeQuery = true
        )
        fun existsByNameIncludingDeleted(@Param("name") name: String): Boolean
    }