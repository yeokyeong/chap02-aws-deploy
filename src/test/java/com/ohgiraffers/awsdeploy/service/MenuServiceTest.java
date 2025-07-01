package com.ohgiraffers.awsdeploy.service;

import com.ohgiraffers.awsdeploy.dto.MenuDTO;
import com.ohgiraffers.awsdeploy.entity.Category;
import com.ohgiraffers.awsdeploy.entity.Menu;
import com.ohgiraffers.awsdeploy.repository.CategoryRepository;
import com.ohgiraffers.awsdeploy.repository.MenuRepository;
import com.ohgiraffers.awsdeploy.util.FileUploadUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * MenuService 단위 테스트
 * 비즈니스 로직을 테스트한다
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuService 테스트")
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileUploadUtils fileUploadUtils;

    @Mock
    private MultipartFile imageFile;

    @InjectMocks
    private MenuService menuService;

    @Test
    @DisplayName("주문 가능한 메뉴 조회 시 DTO 목록이 정상 반환된다")
    void should_ReturnMenuDTOList_when_FindAllOrderableMenus() {
        // given: Repository에서 반환할 메뉴 엔티티들을 준비한다
        Category category = new Category("음료");
        category.setCategoryCode(1L);

        Menu menu1 = new Menu();
        menu1.setMenuCode(1L);
        menu1.setMenuName("아메리카노");
        menu1.setMenuPrice(3000);
        menu1.setMenuDescription("쓴맛 커피");
        menu1.setMenuOrderable("Y");
        menu1.setCategory(category);
        menu1.setMenuStock(10);

        Menu menu2 = new Menu();
        menu2.setMenuCode(2L);
        menu2.setMenuName("라떼");
        menu2.setMenuPrice(4000);
        menu2.setMenuDescription("부드러운 커피");
        menu2.setMenuOrderable("Y");
        menu2.setCategory(category);
        menu2.setMenuStock(15);

        List<Menu> menus = Arrays.asList(menu1, menu2);
        given(menuRepository.findOrderableMenus()).willReturn(menus);

        // when: 주문 가능한 메뉴를 조회한다
        List<MenuDTO> result = menuService.findAllOrderableMenus();

        // then: DTO 목록이 정상 반환된다
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMenuCode()).isEqualTo(1L);
        assertThat(result.get(0).getMenuName()).isEqualTo("아메리카노");
        assertThat(result.get(1).getMenuCode()).isEqualTo(2L);
        assertThat(result.get(1).getMenuName()).isEqualTo("라떼");

        verify(menuRepository).findOrderableMenus();
    }

    @Test
    @DisplayName("메뉴 코드로 조회 시 해당 메뉴 DTO가 정상 반환된다")
    void should_ReturnMenuDTO_when_FindMenuByCode() {
        // given: Repository에서 반환할 메뉴 엔티티를 준비한다
        Long menuCode = 1L;
        Category category = new Category("디저트");
        category.setCategoryCode(2L);

        Menu menu = new Menu();
        menu.setMenuCode(menuCode);
        menu.setMenuName("케이크");
        menu.setMenuPrice(8000);
        menu.setMenuDescription("달콤한 케이크");
        menu.setMenuOrderable("Y");
        menu.setCategory(category);
        menu.setMenuStock(5);

        given(menuRepository.findById(menuCode)).willReturn(Optional.of(menu));

        // when: 메뉴 코드로 메뉴를 조회한다
        MenuDTO result = menuService.findMenuByCode(menuCode);

        // then: 해당 메뉴 DTO가 정상 반환된다
        assertThat(result.getMenuCode()).isEqualTo(1L);
        assertThat(result.getMenuName()).isEqualTo("케이크");
        assertThat(result.getMenuPrice()).isEqualTo(8000);
        assertThat(result.getMenuDescription()).isEqualTo("달콤한 케이크");
        assertThat(result.getCategory().getCategoryCode()).isEqualTo(2L);

        verify(menuRepository).findById(menuCode);
    }

    @Test
    @DisplayName("존재하지 않는 메뉴 코드로 조회 시 예외가 발생한다")
    void should_ThrowException_when_FindMenuByNonExistentCode() {
        // given: Repository에서 빈 결과를 반환하도록 설정한다
        Long nonExistentCode = 999L;
        given(menuRepository.findById(nonExistentCode)).willReturn(Optional.empty());

        // when & then: 존재하지 않는 메뉴 코드로 조회 시 예외가 발생한다
        assertThatThrownBy(() -> menuService.findMenuByCode(nonExistentCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 메뉴를 찾을 수 없습니다: " + nonExistentCode);

        verify(menuRepository).findById(nonExistentCode);
    }

    @Test
    @DisplayName("이미지 파일과 함께 메뉴 등록 시 정상적으로 등록된다")
    void should_RegisterMenuWithImage_when_RegisterMenuWithImageFile() throws Exception {
        // given: 메뉴 등록에 필요한 데이터를 준비한다
        String menuName = "새로운메뉴";
        Integer menuPrice = 10000;
        String menuDescription = "맛있는 메뉴";
        Long categoryCode = 1L;
        Integer menuStock = 20;
        String uploadedFileName = "uploaded-image.jpg";

        Category category = new Category("식사");
        category.setCategoryCode(categoryCode);

        Menu savedMenu = new Menu();
        savedMenu.setMenuCode(1L);
        savedMenu.setMenuName(menuName);
        savedMenu.setMenuPrice(menuPrice);
        savedMenu.setMenuDescription(menuDescription);
        savedMenu.setMenuOrderable("Y");
        savedMenu.setCategory(category);
        savedMenu.setMenuImageUrl(uploadedFileName);
        savedMenu.setMenuStock(menuStock);

        given(categoryRepository.findById(categoryCode)).willReturn(Optional.of(category));
        given(imageFile.isEmpty()).willReturn(false);
        given(fileUploadUtils.isImageFile(imageFile)).willReturn(true);
        given(fileUploadUtils.uploadFile(imageFile)).willReturn(uploadedFileName);
        given(menuRepository.save(any(Menu.class))).willReturn(savedMenu);

        // when: 이미지와 함께 메뉴를 등록한다
        MenuDTO result = menuService.registerMenu(menuName, menuPrice, menuDescription, 
                                                 categoryCode, menuStock, imageFile);

        // then: 메뉴가 정상적으로 등록된다
        assertThat(result.getMenuCode()).isEqualTo(1L);
        assertThat(result.getMenuName()).isEqualTo(menuName);
        assertThat(result.getMenuImageUrl()).isEqualTo(uploadedFileName);

        verify(categoryRepository).findById(categoryCode);
        verify(fileUploadUtils).uploadFile(imageFile);
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("이미지 파일 없이 메뉴 등록 시 정상적으로 등록된다")
    void should_RegisterMenuWithoutImage_when_RegisterMenuWithoutImageFile() {
        // given: 이미지 파일 없는 메뉴 등록 데이터를 준비한다
        String menuName = "이미지없는메뉴";
        Integer menuPrice = 5000;
        String menuDescription = "간단한 메뉴";
        Long categoryCode = 2L;
        Integer menuStock = 10;

        Category category = new Category("음료");
        category.setCategoryCode(categoryCode);

        Menu savedMenu = new Menu();
        savedMenu.setMenuCode(2L);
        savedMenu.setMenuName(menuName);
        savedMenu.setMenuPrice(menuPrice);
        savedMenu.setMenuDescription(menuDescription);
        savedMenu.setMenuOrderable("Y");
        savedMenu.setCategory(category);
        savedMenu.setMenuStock(menuStock);

        given(categoryRepository.findById(categoryCode)).willReturn(Optional.of(category));
        given(menuRepository.save(any(Menu.class))).willReturn(savedMenu);

        // when: 이미지 파일 없이 메뉴를 등록한다
        MenuDTO result = menuService.registerMenu(menuName, menuPrice, menuDescription, 
                                                 categoryCode, menuStock, null);

        // then: 메뉴가 정상적으로 등록된다
        assertThat(result.getMenuCode()).isEqualTo(2L);
        assertThat(result.getMenuName()).isEqualTo(menuName);
        assertThat(result.getMenuImageUrl()).isNull();

        verify(categoryRepository).findById(categoryCode);
        verify(menuRepository).save(any(Menu.class));
        verifyNoInteractions(fileUploadUtils);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 메뉴 등록 시 예외가 발생한다")
    void should_ThrowException_when_RegisterMenuWithNonExistentCategory() {
        // given: 존재하지 않는 카테고리 코드를 준비한다
        Long nonExistentCategoryCode = 999L;
        given(categoryRepository.findById(nonExistentCategoryCode)).willReturn(Optional.empty());

        // when & then: 존재하지 않는 카테고리로 메뉴 등록 시 예외가 발생한다
        assertThatThrownBy(() -> menuService.registerMenu("메뉴", 1000, "설명", 
                                                         nonExistentCategoryCode, 10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 카테고리를 찾을 수 없습니다: " + nonExistentCategoryCode);

        verify(categoryRepository).findById(nonExistentCategoryCode);
        verifyNoInteractions(menuRepository);
    }

    @Test
    @DisplayName("메뉴 삭제 시 이미지 파일도 함께 삭제된다")
    void should_DeleteMenuAndImage_when_DeleteMenuWithImage() {
        // given: 이미지가 있는 메뉴를 준비한다
        Long menuCode = 1L;
        String imageFileName = "menu-image.jpg";

        Menu menu = new Menu();
        menu.setMenuCode(menuCode);
        menu.setMenuName("삭제할메뉴");
        menu.setMenuImageUrl(imageFileName);

        given(menuRepository.findById(menuCode)).willReturn(Optional.of(menu));
        given(fileUploadUtils.deleteFile(imageFileName)).willReturn(true);

        // when: 메뉴를 삭제한다
        menuService.deleteMenu(menuCode);

        // then: 메뉴와 이미지가 함께 삭제된다
        verify(menuRepository).findById(menuCode);
        verify(fileUploadUtils).deleteFile(imageFileName);
        verify(menuRepository).delete(menu);
    }

    @Test
    @DisplayName("존재하지 않는 메뉴 삭제 시 예외가 발생한다")
    void should_ThrowException_when_DeleteNonExistentMenu() {
        // given: 존재하지 않는 메뉴 코드를 준비한다
        Long nonExistentCode = 999L;
        given(menuRepository.findById(nonExistentCode)).willReturn(Optional.empty());

        // when & then: 존재하지 않는 메뉴 삭제 시 예외가 발생한다
        assertThatThrownBy(() -> menuService.deleteMenu(nonExistentCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 메뉴를 찾을 수 없습니다: " + nonExistentCode);

        verify(menuRepository).findById(nonExistentCode);
        verify(menuRepository, never()).delete(any());
        verifyNoInteractions(fileUploadUtils);
    }
} 