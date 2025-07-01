package com.ohgiraffers.awsdeploy.service;

import com.ohgiraffers.awsdeploy.dto.CategoryDTO;
import com.ohgiraffers.awsdeploy.entity.Category;
import com.ohgiraffers.awsdeploy.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * CategoryService 단위 테스트
 * 비즈니스 로직을 테스트한다
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 테스트")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("모든 카테고리 조회 시 DTO 목록이 정상 반환된다")
    void should_ReturnCategoryDTOList_when_FindAllCategories() {
        // given: Repository에서 반환할 카테고리 엔티티들을 준비한다
        Category category1 = new Category("식사");
        category1.setCategoryCode(1L);
        
        Category category2 = new Category("음료");
        category2.setCategoryCode(2L);
        
        Category category3 = new Category("디저트");
        category3.setCategoryCode(3L);
        
        List<Category> categories = Arrays.asList(category1, category2, category3);
        given(categoryRepository.findAll()).willReturn(categories);

        // when: 모든 카테고리를 조회한다
        List<CategoryDTO> result = categoryService.findAllCategories();

        // then: DTO 목록이 정상 반환된다
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCategoryCode()).isEqualTo(1L);
        assertThat(result.get(0).getCategoryName()).isEqualTo("식사");
        assertThat(result.get(1).getCategoryCode()).isEqualTo(2L);
        assertThat(result.get(1).getCategoryName()).isEqualTo("음료");
        assertThat(result.get(2).getCategoryCode()).isEqualTo(3L);
        assertThat(result.get(2).getCategoryName()).isEqualTo("디저트");
        
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("카테고리가 없을 때 빈 목록이 반환된다")
    void should_ReturnEmptyList_when_NoCategoriesExist() {
        // given: Repository에서 빈 목록을 반환하도록 설정한다
        given(categoryRepository.findAll()).willReturn(Arrays.asList());

        // when: 모든 카테고리를 조회한다
        List<CategoryDTO> result = categoryService.findAllCategories();

        // then: 빈 목록이 반환된다
        assertThat(result).isEmpty();
        verify(categoryRepository).findAll();
    }
} 