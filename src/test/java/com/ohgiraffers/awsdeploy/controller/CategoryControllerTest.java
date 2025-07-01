package com.ohgiraffers.awsdeploy.controller;

import com.ohgiraffers.awsdeploy.dto.CategoryDTO;
import com.ohgiraffers.awsdeploy.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CategoryController 웹 계층 테스트
 * HTTP 요청/응답과 컨트롤러 로직을 테스트한다
 */
@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController 테스트")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 목록 조회 API 호출 시 JSON 응답이 정상 반환된다")
    void should_ReturnCategoriesAsJson_when_GetCategories() throws Exception {
        // given: Service에서 반환할 카테고리 DTO 목록을 준비한다
        CategoryDTO category1 = new CategoryDTO(1L, "식사");
        CategoryDTO category2 = new CategoryDTO(2L, "음료");
        CategoryDTO category3 = new CategoryDTO(3L, "디저트");
        
        List<CategoryDTO> categories = Arrays.asList(category1, category2, category3);
        given(categoryService.findAllCategories()).willReturn(categories);

        // when & then: GET /api/categories 호출 시 JSON 응답이 정상 반환된다
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].categoryCode").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("식사"))
                .andExpect(jsonPath("$[1].categoryCode").value(2))
                .andExpect(jsonPath("$[1].categoryName").value("음료"))
                .andExpect(jsonPath("$[2].categoryCode").value(3))
                .andExpect(jsonPath("$[2].categoryName").value("디저트"));
    }

    @Test
    @DisplayName("카테고리가 없을 때 빈 배열이 정상 반환된다")
    void should_ReturnEmptyArray_when_NoCategoriesExist() throws Exception {
        // given: Service에서 빈 목록을 반환하도록 설정한다
        given(categoryService.findAllCategories()).willReturn(Arrays.asList());

        // when & then: GET /api/categories 호출 시 빈 배열이 반환된다
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
} 