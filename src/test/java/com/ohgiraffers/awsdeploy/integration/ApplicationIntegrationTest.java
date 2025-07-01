package com.ohgiraffers.awsdeploy.integration;

import com.ohgiraffers.awsdeploy.dto.CategoryDTO;
import com.ohgiraffers.awsdeploy.dto.MenuDTO;
import com.ohgiraffers.awsdeploy.entity.Category;
import com.ohgiraffers.awsdeploy.repository.CategoryRepository;
import com.ohgiraffers.awsdeploy.service.CategoryService;
import com.ohgiraffers.awsdeploy.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 애플리케이션 통합 테스트
 * 
 * 전체 Spring Boot 애플리케이션 컨텍스트가 정상적으로 로딩되는지 확인하고
 * 주요 서비스들이 기본 데이터와 함께 동작하는지 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("애플리케이션 통합 테스트")
class ApplicationIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MenuService menuService;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Spring Boot 애플리케이션 컨텍스트가 정상적으로 로딩된다")
    void should_LoadApplicationContext() {
        // Given & When: 애플리케이션 컨텍스트 로딩

        // Then: 주요 빈들이 정상적으로 주입되었는지 확인
        assertThat(categoryService).isNotNull();
        assertThat(menuService).isNotNull();
    }

    @Test
    @DisplayName("카테고리 서비스가 정상적으로 동작한다")
    void should_WorkCategoryServiceCorrectly() {
        // Given: 테스트용 카테고리 데이터 (H2 인메모리 DB는 초기 데이터 없음)

        // When: 전체 카테고리 조회
        List<CategoryDTO> categories = categoryService.findAllCategories();

        // Then: 빈 목록이지만 서비스는 정상 동작한다
        assertThat(categories).isNotNull();
        assertThat(categories).isEmpty(); // H2 인메모리 DB는 초기 데이터 없음
    }

    @Test
    @DisplayName("메뉴 서비스가 정상적으로 동작한다") 
    void should_WorkMenuServiceCorrectly() {
        // Given: H2 인메모리 DB에는 초기 데이터가 없음

        // When: 전체 메뉴 조회
        List<MenuDTO> menus = menuService.findAllOrderableMenus();

        // Then: 빈 목록이지만 서비스는 정상 동작한다
        assertThat(menus).isNotNull();
        assertThat(menus).isEmpty(); // H2 인메모리 DB는 초기 데이터 없음
    }

    @Test
    @DisplayName("메뉴 등록과 삭제가 정상적으로 동작한다")
    void should_RegisterAndDeleteMenuCorrectly() {
        // Given: 테스트용 카테고리를 먼저 생성
        Category savedCategory = categoryRepository.save(new Category("테스트카테고리"));
        CategoryDTO testCategory = new CategoryDTO(savedCategory.getCategoryCode(), savedCategory.getCategoryName());
        int initialMenuCount = menuService.findAllOrderableMenus().size();

        // When: 메뉴 등록 (이미지 없이)
        MenuDTO newMenu = menuService.registerMenu(
                "통합테스트메뉴", 
                15000, 
                "통합 테스트용 메뉴입니다", 
                testCategory.getCategoryCode(), 
                10, 
                null  // 이미지 없음
        );

        // Then: 메뉴가 정상 등록되고 목록에 추가됨
        assertThat(newMenu).isNotNull();
        assertThat(newMenu.getMenuName()).isEqualTo("통합테스트메뉴");
        assertThat(menuService.findAllOrderableMenus()).hasSize(initialMenuCount + 1);

        // When: 등록된 메뉴 삭제
        menuService.deleteMenu(newMenu.getMenuCode());

        // Then: 메뉴가 정상 삭제되고 원래 개수로 복원됨
        assertThat(menuService.findAllOrderableMenus()).hasSize(initialMenuCount);
    }

    @Test
    @DisplayName("서비스 계층의 비즈니스 로직이 정상적으로 동작한다")
    void should_WorkServiceLayerCorrectly() {
        // Given: 테스트용 카테고리 생성
        Category savedCategory = categoryRepository.save(new Category("서비스테스트카테고리"));
        CategoryDTO testCategory = new CategoryDTO(savedCategory.getCategoryCode(), savedCategory.getCategoryName());
        
        // When: 메뉴 등록
        MenuDTO newMenu = menuService.registerMenu(
                "서비스테스트메뉴", 
                25000, 
                "서비스 계층 테스트", 
                testCategory.getCategoryCode(), 
                3, 
                null
        );
        
        // Then: 메뉴가 정상 등록됨
        assertThat(newMenu).isNotNull();
        assertThat(newMenu.getMenuName()).isEqualTo("서비스테스트메뉴");
        assertThat(newMenu.getMenuPrice()).isEqualTo(25000);
        assertThat(newMenu.getCategory().getCategoryName()).isEqualTo("서비스테스트카테고리");
        
        // When: 등록된 메뉴 조회
        MenuDTO foundMenu = menuService.findMenuByCode(newMenu.getMenuCode());
        
        // Then: 조회된 메뉴가 등록한 메뉴와 일치함
        assertThat(foundMenu.getMenuCode()).isEqualTo(newMenu.getMenuCode());
        assertThat(foundMenu.getMenuName()).isEqualTo("서비스테스트메뉴");
    }
} 