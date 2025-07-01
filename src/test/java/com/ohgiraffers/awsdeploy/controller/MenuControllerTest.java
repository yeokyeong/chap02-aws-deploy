package com.ohgiraffers.awsdeploy.controller;

import com.ohgiraffers.awsdeploy.dto.CategoryDTO;
import com.ohgiraffers.awsdeploy.dto.MenuDTO;
import com.ohgiraffers.awsdeploy.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MenuController 웹 계층 테스트
 * HTTP 요청/응답과 컨트롤러 로직을 테스트한다
 */
@WebMvcTest(MenuController.class)
@DisplayName("MenuController 테스트")
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Test
    @DisplayName("메뉴 목록 조회 API 호출 시 JSON 응답이 정상 반환된다")
    void should_ReturnMenusAsJson_when_GetMenus() throws Exception {
        // given: Service에서 반환할 메뉴 DTO 목록을 준비한다
        CategoryDTO category = new CategoryDTO(1L, "음료");
        
        MenuDTO menu1 = new MenuDTO();
        menu1.setMenuCode(1L);
        menu1.setMenuName("아메리카노");
        menu1.setMenuPrice(3000);
        menu1.setMenuDescription("쓴맛 커피");
        menu1.setCategory(category);
        menu1.setMenuStock(10);

        MenuDTO menu2 = new MenuDTO();
        menu2.setMenuCode(2L);
        menu2.setMenuName("라떼");
        menu2.setMenuPrice(4000);
        menu2.setMenuDescription("부드러운 커피");
        menu2.setCategory(category);
        menu2.setMenuStock(15);

        List<MenuDTO> menus = Arrays.asList(menu1, menu2);
        given(menuService.findAllOrderableMenus()).willReturn(menus);

        // when & then: GET /api/menus 호출 시 JSON 응답이 정상 반환된다
        mockMvc.perform(get("/api/menus")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].menuCode").value(1))
                .andExpect(jsonPath("$[0].menuName").value("아메리카노"))
                .andExpect(jsonPath("$[0].menuPrice").value(3000))
                .andExpect(jsonPath("$[1].menuCode").value(2))
                .andExpect(jsonPath("$[1].menuName").value("라떼"))
                .andExpect(jsonPath("$[1].menuPrice").value(4000));
    }

    @Test
    @DisplayName("특정 메뉴 조회 API 호출 시 해당 메뉴 정보가 정상 반환된다")
    void should_ReturnMenuDetail_when_GetMenuById() throws Exception {
        // given: Service에서 반환할 메뉴 DTO를 준비한다
        Long menuCode = 1L;
        CategoryDTO category = new CategoryDTO(2L, "디저트");
        
        MenuDTO menu = new MenuDTO();
        menu.setMenuCode(menuCode);
        menu.setMenuName("케이크");
        menu.setMenuPrice(8000);
        menu.setMenuDescription("달콤한 케이크");
        menu.setCategory(category);
        menu.setMenuStock(5);
        menu.setMenuImageUrl("cake.jpg");

        given(menuService.findMenuByCode(menuCode)).willReturn(menu);

        // when & then: GET /api/menus/{id} 호출 시 메뉴 정보가 정상 반환된다
        mockMvc.perform(get("/api/menus/{id}", menuCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menuCode").value(1))
                .andExpect(jsonPath("$.menuName").value("케이크"))
                .andExpect(jsonPath("$.menuPrice").value(8000))
                .andExpect(jsonPath("$.menuDescription").value("달콤한 케이크"))
                .andExpect(jsonPath("$.menuImageUrl").value("cake.jpg"))
                .andExpect(jsonPath("$.category.categoryCode").value(2))
                .andExpect(jsonPath("$.category.categoryName").value("디저트"));
    }

    @Test
    @DisplayName("메뉴 등록 API 호출 시 정상적으로 등록된다")
    void should_RegisterMenu_when_PostMenu() throws Exception {
        // given: 등록할 메뉴 데이터와 등록된 결과를 준비한다
        String menuName = "새로운메뉴";
        Integer menuPrice = 10000;
        String menuDescription = "맛있는 메뉴";
        Long categoryCode = 1L;
        Integer menuStock = 20;
        
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "test-image.jpg", "image/jpeg", "test image content".getBytes());

        CategoryDTO category = new CategoryDTO(categoryCode, "식사");
        MenuDTO registeredMenu = new MenuDTO();
        registeredMenu.setMenuCode(1L);
        registeredMenu.setMenuName(menuName);
        registeredMenu.setMenuPrice(menuPrice);
        registeredMenu.setMenuDescription(menuDescription);
        registeredMenu.setCategory(category);
        registeredMenu.setMenuStock(menuStock);
        registeredMenu.setMenuImageUrl("uploaded-image.jpg");

        given(menuService.registerMenu(eq(menuName), eq(menuPrice), eq(menuDescription),
                eq(categoryCode), eq(menuStock), any())).willReturn(registeredMenu);

        // when & then: POST /api/menus 호출 시 메뉴가 정상 등록된다
        mockMvc.perform(multipart("/api/menus")
                        .file(imageFile)
                        .param("menuName", menuName)
                        .param("menuPrice", menuPrice.toString())
                        .param("menuDescription", menuDescription)
                        .param("categoryCode", categoryCode.toString())
                        .param("menuStock", menuStock.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menuCode").value(1))
                .andExpect(jsonPath("$.menuName").value(menuName))
                .andExpect(jsonPath("$.menuPrice").value(menuPrice))
                .andExpect(jsonPath("$.menuImageUrl").value("uploaded-image.jpg"));

        verify(menuService).registerMenu(eq(menuName), eq(menuPrice), eq(menuDescription),
                eq(categoryCode), eq(menuStock), any());
    }

    @Test
    @DisplayName("메뉴 삭제 API 호출 시 정상적으로 삭제된다")
    void should_DeleteMenu_when_DeleteMenuById() throws Exception {
        // given: 삭제할 메뉴 코드를 준비한다
        Long menuCode = 1L;

        // when & then: DELETE /api/menus/{id} 호출 시 메뉴가 정상 삭제된다
        mockMvc.perform(delete("/api/menus/{id}", menuCode))
                .andExpect(status().isOk());

        verify(menuService).deleteMenu(menuCode);
    }

    @Test
    @DisplayName("존재하지 않는 메뉴 조회 시 404 오류가 반환된다")
    void should_Return404_when_GetNonExistentMenu() throws Exception {
        // given: Service에서 예외를 던지도록 설정한다
        Long nonExistentCode = 999L;
        given(menuService.findMenuByCode(nonExistentCode))
                .willThrow(new IllegalArgumentException("메뉴를 찾을 수 없습니다. 메뉴 코드: " + nonExistentCode));

        // when & then: GET /api/menus/{id} 호출 시 404 오류가 반환된다
        mockMvc.perform(get("/api/menus/{id}", nonExistentCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
} 