package com.ohgiraffers.awsdeploy.repository;

import com.ohgiraffers.awsdeploy.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CategoryRepository 단위 테스트
 * JPA 레이어만 테스트하는 슬라이스 테스트
 */
@DataJpaTest
@DisplayName("CategoryRepository 테스트")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("모든 카테고리 조회 시 등록된 카테고리들이 정상 조회된다")
    void should_ReturnAllCategories_when_FindAll() {
        // given: 테스트용 카테고리 데이터를 준비한다
        Category category1 = new Category();
        category1.setCategoryName("식사");
        
        Category category2 = new Category();
        category2.setCategoryName("음료");
        
        entityManager.persistAndFlush(category1);
        entityManager.persistAndFlush(category2);

        // when: 모든 카테고리를 조회한다
        List<Category> categories = categoryRepository.findAll();

        // then: 등록된 카테고리들이 모두 조회된다
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting("categoryName")
                .containsExactlyInAnyOrder("식사", "음료");
    }

    @Test
    @DisplayName("ID로 카테고리 조회 시 해당 카테고리가 정상 조회된다")
    void should_ReturnCategory_when_FindById() {
        // given: 테스트용 카테고리를 등록한다
        Category category = new Category();
        category.setCategoryName("디저트");
        Category savedCategory = entityManager.persistAndFlush(category);

        // when: ID로 카테고리를 조회한다
        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getCategoryCode());

        // then: 해당 카테고리가 정상 조회된다
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getCategoryName()).isEqualTo("디저트");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 카테고리 조회 시 빈 결과가 반환된다")
    void should_ReturnEmpty_when_FindByNonExistentId() {
        // given: 존재하지 않는 카테고리 ID를 준비한다
        Long nonExistentId = 999L;

        // when: 존재하지 않는 ID로 카테고리를 조회한다
        Optional<Category> foundCategory = categoryRepository.findById(nonExistentId);

        // then: 빈 결과가 반환된다
        assertThat(foundCategory).isEmpty();
    }

    @Test
    @DisplayName("새로운 카테고리 저장 시 정상적으로 저장된다")
    void should_SaveCategory_when_SaveNewCategory() {
        // given: 새로운 카테고리를 준비한다
        Category newCategory = new Category();
        newCategory.setCategoryName("스낵");

        // when: 카테고리를 저장한다
        Category savedCategory = categoryRepository.save(newCategory);

        // then: 카테고리가 정상적으로 저장된다
        assertThat(savedCategory.getCategoryCode()).isNotNull();
        assertThat(savedCategory.getCategoryName()).isEqualTo("스낵");
        
        // 실제 DB에서 조회했을 때도 존재한다
        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getCategoryCode());
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getCategoryName()).isEqualTo("스낵");
    }
} 