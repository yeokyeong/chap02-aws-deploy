package com.ohgiraffers.awsdeploy.repository;

import com.ohgiraffers.awsdeploy.entity.Category;
import com.ohgiraffers.awsdeploy.entity.Menu;
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
 * MenuRepository 단위 테스트
 * JPA 레이어만 테스트하는 슬라이스 테스트
 */
@DataJpaTest
@DisplayName("MenuRepository 테스트")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MenuRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MenuRepository menuRepository;

    @Test
    @DisplayName("주문 가능한 메뉴 조회 시 최신순으로 정렬되어 조회된다")
    void should_ReturnOrderableMenusInDescOrder_when_FindOrderableMenus() {
        // given: 카테고리와 주문 가능/불가능한 메뉴들을 준비한다
        Category category = new Category("음료");
        entityManager.persistAndFlush(category);

        Menu menu1 = new Menu();
        menu1.setMenuName("라떼");
        menu1.setMenuPrice(4000);
        menu1.setMenuOrderable("Y");
        menu1.setCategory(category);
        menu1.setMenuStock(10);
        entityManager.persistAndFlush(menu1);

        Menu menu2 = new Menu();
        menu2.setMenuName("아메리카노");
        menu2.setMenuPrice(3000);
        menu2.setMenuOrderable("Y");
        menu2.setCategory(category);
        menu2.setMenuStock(15);
        entityManager.persistAndFlush(menu2);

        Menu menu3 = new Menu();
        menu3.setMenuName("품절메뉴");
        menu3.setMenuPrice(5000);
        menu3.setMenuOrderable("N");
        menu3.setCategory(category);
        menu3.setMenuStock(0);
        entityManager.persistAndFlush(menu3);

        // when: 주문 가능한 메뉴를 조회한다
        List<Menu> orderableMenus = menuRepository.findOrderableMenus();

        // then: 주문 가능한 메뉴만 최신순(menuCode 역순)으로 조회된다
        assertThat(orderableMenus).hasSize(2);
        assertThat(orderableMenus.get(0).getMenuName()).isEqualTo("아메리카노"); // 더 나중에 저장된 메뉴
        assertThat(orderableMenus.get(1).getMenuName()).isEqualTo("라떼");
        assertThat(orderableMenus).allMatch(menu -> "Y".equals(menu.getMenuOrderable()));
    }

    @Test
    @DisplayName("카테고리별 메뉴 조회 시 해당 카테고리의 주문 가능한 메뉴만 조회된다")
    void should_ReturnMenusByCategory_when_FindMenusByCategoryCode() {
        // given: 여러 카테고리와 메뉴들을 준비한다
        Category category1 = new Category("음료");
        Category category2 = new Category("식사");
        entityManager.persistAndFlush(category1);
        entityManager.persistAndFlush(category2);

        Menu menu1 = new Menu();
        menu1.setMenuName("커피");
        menu1.setMenuPrice(4000);
        menu1.setMenuOrderable("Y");
        menu1.setCategory(category1);
        menu1.setMenuStock(10);
        entityManager.persistAndFlush(menu1);

        Menu menu2 = new Menu();
        menu2.setMenuName("파스타");
        menu2.setMenuPrice(12000);
        menu2.setMenuOrderable("Y");
        menu2.setCategory(category2);
        menu2.setMenuStock(5);
        entityManager.persistAndFlush(menu2);

        // when: 첫 번째 카테고리의 메뉴를 조회한다
        List<Menu> categoryMenus = menuRepository.findMenusByCategoryCode(category1.getCategoryCode());

        // then: 해당 카테고리의 메뉴만 조회된다
        assertThat(categoryMenus).hasSize(1);
        assertThat(categoryMenus.get(0).getMenuName()).isEqualTo("커피");
        assertThat(categoryMenus.get(0).getCategory().getCategoryCode()).isEqualTo(category1.getCategoryCode());
    }

    @Test
    @DisplayName("ID로 메뉴 조회 시 해당 메뉴가 정상 조회된다")
    void should_ReturnMenu_when_FindById() {
        // given: 테스트용 메뉴를 등록한다
        Category category = new Category("디저트");
        entityManager.persistAndFlush(category);

        Menu menu = new Menu();
        menu.setMenuName("케이크");
        menu.setMenuPrice(8000);
        menu.setMenuDescription("달콤한 케이크");
        menu.setMenuOrderable("Y");
        menu.setCategory(category);
        menu.setMenuStock(3);
        Menu savedMenu = entityManager.persistAndFlush(menu);

        // when: ID로 메뉴를 조회한다
        Optional<Menu> foundMenu = menuRepository.findById(savedMenu.getMenuCode());

        // then: 해당 메뉴가 정상 조회된다
        assertThat(foundMenu).isPresent();
        assertThat(foundMenu.get().getMenuName()).isEqualTo("케이크");
        assertThat(foundMenu.get().getMenuPrice()).isEqualTo(8000);
        assertThat(foundMenu.get().getMenuDescription()).isEqualTo("달콤한 케이크");
    }

    @Test
    @DisplayName("새로운 메뉴 저장 시 정상적으로 저장된다")
    void should_SaveMenu_when_SaveNewMenu() {
        // given: 카테고리와 새로운 메뉴를 준비한다
        Category category = new Category("스낵");
        entityManager.persistAndFlush(category);

        Menu newMenu = new Menu();
        newMenu.setMenuName("감자칩");
        newMenu.setMenuPrice(2000);
        newMenu.setMenuDescription("바삭한 감자칩");
        newMenu.setMenuOrderable("Y");
        newMenu.setCategory(category);
        newMenu.setMenuStock(20);

        // when: 메뉴를 저장한다
        Menu savedMenu = menuRepository.save(newMenu);

        // then: 메뉴가 정상적으로 저장된다
        assertThat(savedMenu.getMenuCode()).isNotNull();
        assertThat(savedMenu.getMenuName()).isEqualTo("감자칩");
        assertThat(savedMenu.getCategory().getCategoryName()).isEqualTo("스낵");
        
        // 실제 DB에서 조회했을 때도 존재한다
        Optional<Menu> foundMenu = menuRepository.findById(savedMenu.getMenuCode());
        assertThat(foundMenu).isPresent();
        assertThat(foundMenu.get().getMenuName()).isEqualTo("감자칩");
    }

    @Test
    @DisplayName("메뉴 삭제 시 정상적으로 삭제된다")
    void should_DeleteMenu_when_DeleteById() {
        // given: 테스트용 메뉴를 등록한다
        Category category = new Category("음료");
        entityManager.persistAndFlush(category);

        Menu menu = new Menu();
        menu.setMenuName("삭제할메뉴");
        menu.setMenuPrice(1000);
        menu.setMenuOrderable("Y");
        menu.setCategory(category);
        menu.setMenuStock(1);
        Menu savedMenu = entityManager.persistAndFlush(menu);
        Long menuId = savedMenu.getMenuCode();

        // when: 메뉴를 삭제한다
        menuRepository.deleteById(menuId);
        entityManager.flush();

        // then: 메뉴가 정상적으로 삭제된다
        Optional<Menu> deletedMenu = menuRepository.findById(menuId);
        assertThat(deletedMenu).isEmpty();
    }
} 