const API_BASE_URL = '/api';

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
    loadMenus();
});

// 카테고리 목록 로드
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE_URL}/categories`);
        const categories = await response.json();
        
        const categorySelect = document.getElementById('categoryCode');
        categorySelect.innerHTML = '<option value="">카테고리를 선택하세요</option>';
        
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.categoryCode;
            option.textContent = category.categoryName;
            categorySelect.appendChild(option);
        });
    } catch (error) {
        console.error('카테고리 로드 실패:', error);
    }
}

// 메뉴 목록 로드
async function loadMenus() {
    const container = document.getElementById('menuContainer');
    
    try {
        const response = await fetch(`${API_BASE_URL}/menus`);
        const menus = await response.json();
        
        if (menus.length === 0) {
            container.innerHTML = '<div class="error">등록된 메뉴가 없습니다.</div>';
            return;
        }
        
        const menuGrid = document.createElement('div');
        menuGrid.className = 'menu-grid';
        
        menus.forEach(menu => {
            const menuCard = createMenuCard(menu);
            menuGrid.appendChild(menuCard);
        });
        
        container.innerHTML = '';
        container.appendChild(menuGrid);
        
    } catch (error) {
        console.error('메뉴 로드 실패:', error);
        container.innerHTML = '<div class="error">메뉴를 불러오는데 실패했습니다.</div>';
    }
}

// 메뉴 카드 생성
function createMenuCard(menu) {
    const card = document.createElement('div');
    card.className = 'menu-card';
    
    // S3 완전 URL인지 로컬 파일 경로인지 판단하여 이미지 URL 생성
    const imageUrl = menu.menuImageUrl 
        ? (menu.menuImageUrl.startsWith('https://') 
            ? menu.menuImageUrl 
            : `${API_BASE_URL}/images/${menu.menuImageUrl}`)
        : '';
    
    const formatPrice = (price) => {
        return new Intl.NumberFormat('ko-KR').format(price);
    };
    
    card.innerHTML = `
        <div onclick="openMenuDetailModal(${menu.menuCode})" style="cursor: pointer;">
            ${imageUrl 
                ? `<img src="${imageUrl}" alt="${menu.menuName}" class="menu-image" onerror="this.style.display='none'">` 
                : '<div class="menu-image">이미지 없음</div>'
            }
            <div class="menu-content">
                <div class="menu-category">${menu.category ? menu.category.categoryName : '미분류'}</div>
                <div class="menu-title">${menu.menuName}</div>
                <div class="menu-price">${formatPrice(menu.menuPrice)}원</div>
                <div class="menu-description">${menu.menuDescription || '설명이 없습니다.'}</div>
                <div class="menu-stock">재고: ${menu.menuStock}개</div>
            </div>
        </div>
        <div class="menu-content" style="padding-top: 0;">
            <button class="btn btn-danger delete-btn" onclick="event.stopPropagation(); deleteMenu(${menu.menuCode})">
                🗑️ 삭제
            </button>
        </div>
    `;
    
    return card;
}

// 메뉴 삭제
async function deleteMenu(menuCode) {
    if (!confirm('정말로 이 메뉴를 삭제하시겠습니까?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/menus/${menuCode}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('메뉴가 성공적으로 삭제되었습니다.');
            loadMenus(); // 목록 새로고침
        } else {
            alert('메뉴 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('메뉴 삭제 실패:', error);
        alert('메뉴 삭제 중 오류가 발생했습니다.');
    }
}

// 메뉴 등록 모달 열기
function openAddMenuModal() {
    document.getElementById('addMenuModal').style.display = 'block';
}

// 메뉴 등록 모달 닫기
function closeAddMenuModal() {
    document.getElementById('addMenuModal').style.display = 'none';
    document.getElementById('addMenuForm').reset();
}

// 메뉴 상세 모달 열기
async function openMenuDetailModal(menuCode) {
    try {
        const response = await fetch(`${API_BASE_URL}/menus/${menuCode}`);
        const menu = await response.json();
        
        // S3 완전 URL인지 로컬 파일 경로인지 판단하여 이미지 URL 생성
        const imageUrl = menu.menuImageUrl 
            ? (menu.menuImageUrl.startsWith('https://') 
                ? menu.menuImageUrl 
                : `${API_BASE_URL}/images/${menu.menuImageUrl}`)
            : '';
        
        const formatPrice = (price) => {
            return new Intl.NumberFormat('ko-KR').format(price);
        };
        
        const detailContent = document.getElementById('menuDetailContent');
        detailContent.innerHTML = `
            <div style="text-align: center; margin-bottom: 25px;">
                ${imageUrl 
                    ? `<img src="${imageUrl}" alt="${menu.menuName}" style="max-width: 100%; max-height: 300px; border-radius: 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.1);" onerror="this.style.display='none'">` 
                    : '<div style="width: 100%; height: 200px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: #999; border-radius: 10px;">이미지 없음</div>'
                }
            </div>
            <div style="margin-bottom: 20px;">
                <div style="display: inline-block; background: #e3f2fd; color: #1976d2; padding: 6px 16px; border-radius: 20px; font-size: 0.9rem; font-weight: 600; margin-bottom: 15px;">
                    ${menu.category ? menu.category.categoryName : '미분류'}
                </div>
                <h3 style="font-size: 1.8rem; color: #2c3e50; margin-bottom: 10px;">${menu.menuName}</h3>
                <div style="font-size: 1.5rem; font-weight: 800; color: #667eea; margin-bottom: 15px;">${formatPrice(menu.menuPrice)}원</div>
                <p style="color: #666; line-height: 1.6; margin-bottom: 15px;">${menu.menuDescription || '설명이 없습니다.'}</p>
                <div style="color: #4caf50; font-weight: 600; margin-bottom: 20px;">재고: ${menu.menuStock}개</div>
            </div>
            <div style="display: flex; gap: 10px; justify-content: center;">
                ${menu.menuImageUrl 
                    ? `<button class="btn" onclick="downloadMenuImage('${menu.menuImageUrl}', '${menu.menuName}')" style="background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);">
                        📥 이미지 다운로드
                       </button>` 
                    : ''
                }
                <button class="btn btn-danger" onclick="confirmDeleteMenu(${menu.menuCode})">
                    🗑️ 메뉴 삭제
                </button>
            </div>
        `;
        
        document.getElementById('menuDetailModal').style.display = 'block';
        
    } catch (error) {
        console.error('메뉴 상세 정보 로드 실패:', error);
        alert('메뉴 정보를 불러오는데 실패했습니다.');
    }
}

// 메뉴 상세 모달 닫기
function closeMenuDetailModal() {
    document.getElementById('menuDetailModal').style.display = 'none';
}

// 메뉴 이미지 다운로드
function downloadMenuImage(imageUrl, menuName) {
    const link = document.createElement('a');
    
    // S3 완전 URL인지 로컬 파일 경로인지 판단하여 다운로드 URL 생성
    if (imageUrl.startsWith('https://')) {
        // S3 파일의 경우 직접 S3 URL에서 다운로드
        link.href = imageUrl;
        // S3 URL에서 파일명 추출
        const fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        link.download = `${menuName}_${fileName}`;
    } else {
        // 로컬 파일의 경우 기존 로직 사용
        link.href = `${API_BASE_URL}/images/download/${imageUrl}`;
        link.download = `${menuName}_${imageUrl}`;
    }
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// 상세 모달에서 메뉴 삭제 확인
async function confirmDeleteMenu(menuCode) {
    if (!confirm('정말로 이 메뉴를 삭제하시겠습니까?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/menus/${menuCode}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('메뉴가 성공적으로 삭제되었습니다.');
            closeMenuDetailModal();
            loadMenus(); // 목록 새로고침
        } else {
            alert('메뉴 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('메뉴 삭제 실패:', error);
        alert('메뉴 삭제 중 오류가 발생했습니다.');
    }
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const addModal = document.getElementById('addMenuModal');
    const detailModal = document.getElementById('menuDetailModal');
    
    if (event.target === addModal) {
        closeAddMenuModal();
    }
    if (event.target === detailModal) {
        closeMenuDetailModal();
    }
}

// 메뉴 등록 폼 제출 이벤트 리스너
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('addMenuForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const formData = new FormData(this);
        
        try {
            const response = await fetch(`${API_BASE_URL}/menus`, {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                alert('메뉴가 성공적으로 등록되었습니다.');
                closeAddMenuModal();
                loadMenus(); // 목록 새로고침
            } else {
                alert('메뉴 등록에 실패했습니다.');
            }
        } catch (error) {
            console.error('메뉴 등록 실패:', error);
            alert('메뉴 등록 중 오류가 발생했습니다.');
        }
    });
}); 