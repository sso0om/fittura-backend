package com.fittura.domain.product.categry.entity;

import com.fittura.domain.product.categry.constant.CategoryStatus;
import com.fittura.domain.product.categry.error.CategoryErrorCode;
import com.fittura.domain.product.categry.support.CategoryFixture;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    // ========== 카테고리 생성 ==========

    @Test
    @DisplayName("루트 카테고리 생성 성공")
    void createRootSuccess() {
        // when
        Category category = Category.createRoot("루트", 1);

        // then
        assertThat(category.getName()).isEqualTo("루트");
        assertThat(category.getParent()).isNull();
        assertThat(category.getChildren()).isEmpty();
        assertThat(category.getDepth()).isEqualTo(0);
        assertThat(category.getSortOrder()).isEqualTo(1);
        assertThat(category.getStatus()).isEqualTo(CategoryStatus.DISABLED);
    }

    @Test
    @DisplayName("자식 카테고리 생성 성공")
    void createChildSuccess() {
        // given
        Category parent = Category.createRoot("부모", 1);

        // when
        Category child = Category.createChild("자식", 2, parent);

        // then
        assertThat(child.getName()).isEqualTo("자식");
        assertThat(child.getParent()).isEqualTo(parent);
        assertThat(child.getDepth()).isEqualTo(parent.getDepth() + 1);
        assertThat(child.getSortOrder()).isEqualTo(2);
        assertThat(child.getStatus()).isEqualTo(CategoryStatus.DISABLED);

        assertThat(parent.getChildren()).contains(child);
    }

    @Test
    @DisplayName("자식 카테고리 생성 성공")
    void createChildFailArchivedParent() {
        // given
        Category parent = CategoryFixture.rootActiveWithId(1L);
        ReflectionTestUtils.setField(parent, "status", CategoryStatus.ARCHIVED);

        // when & then
        assertThatThrownBy(() -> Category.createChild("자식", 2, parent))
            .isInstanceOf(ServiceException.class)
            .hasMessage(CategoryErrorCode.NOT_ARCHIVED_PARENT.getMessage());
    }


    // ========== 카테고리 수정 ==========

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateSuccess() {
        // given
        Category category = Category.createRoot("기존", 1);

        // when
        category.update("변경", 2);

        // then
        assertThat(category.getName()).isEqualTo("변경");
        assertThat(category.getSortOrder()).isEqualTo(2);
    }


    // ========== 부모 카테고리 변경 ==========

    @Test
    @DisplayName("부모 카테고리 변경 성공")
    void changeParentSuccess() {
        // given
        Category root1 = Category.createRoot("루트1", 1);
        Category root2 = Category.createRoot("루트2", 2);
        Category child = Category.createChild("자식", 1, root1);

        // when
        child.changeParent(root2);

        // then
        assertThat(child.getParent()).isEqualTo(root2);
        assertThat(child.getDepth()).isEqualTo(root2.getDepth() + 1);

        assertThat(root1.getChildren()).doesNotContain(child);
        assertThat(root2.getChildren()).contains(child);
    }

    @Test
    @DisplayName("루트 카테고리로 변경 성공")
    void changeParentToRootSuccess() {
        // given
        Category root = Category.createRoot("루트", 1);
        Category child = Category.createChild("자식", 1, root);

        // when
        child.changeParent(null);

        // then
        assertThat(child.getParent()).isNull();
        assertThat(child.getDepth()).isEqualTo(0);
        assertThat(root.getChildren()).doesNotContain(child);
    }

    @Test
    @DisplayName("부모 변경 시 하위 카테고리 depth 연쇄 변경 성공")
    void changeParentUpdateDepthRecursively() {
        // given
        Category root1 = Category.createRoot("루트1", 1);

        Category parent = Category.createRoot("중간", 2);
        Category child = Category.createChild("자식", 1, parent);
        Category grandChild = Category.createChild("손자", 1, child);

        // when
        parent.changeParent(root1);

        // then
        assertThat(parent.getParent()).isEqualTo(root1);
        assertThat(parent.getDepth()).isEqualTo(1);
        assertThat(child.getDepth()).isEqualTo(2);
        assertThat(grandChild.getDepth()).isEqualTo(3);
    }

    @Test
    @DisplayName("부모 변경 실패 - 자기 자신을 부모로 지정하면 예외")
    void changeParentFailSelfParent() {
        // given
        Category category = Category.createRoot("루트", 1);

        // when & then
        assertThatThrownBy(() -> category.changeParent(category))
            .isInstanceOf(ServiceException.class)
            .hasMessage(CategoryErrorCode.NOT_SELF_PARENT.getMessage());
    }

    @Test
    @DisplayName("부모 변경 실패 - 자신의 하위를 부모로 지정하면 예외")
    void changeParentFailDescendantParent() {
        // given
        Category root = Category.createRoot("루트", 1);
        Category child = Category.createChild("자식", 1, root);
        Category grandChild = Category.createChild("손자", 1, child);

        // when & then
        assertThatThrownBy(() -> root.changeParent(child))
            .isInstanceOf(ServiceException.class)
            .hasMessage(CategoryErrorCode.NOT_DESCENDANT_PARENT.getMessage());

        assertThatThrownBy(() -> root.changeParent(grandChild))
            .isInstanceOf(ServiceException.class)
            .hasMessage(CategoryErrorCode.NOT_DESCENDANT_PARENT.getMessage());
    }


    // ========== 카테고리 활성화 / 비활성화 ==========

    @Test
    @DisplayName("카테고리 활성화 성공")
    void activateSuccess() {
        // given
        Category category = Category.createRoot("루트", 1);

        // when
        category.activate();

        // then
        assertThat(category.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
    }

    @Test
    @DisplayName("카테고리 활성화 실패 - 부모가 활성화 상태가 아님")
    void activateFailNotActiveParent() {
        // given
        Category root1 = Category.createRoot("루트1", 1);
        Category child1 = CategoryFixture.child("자식1", 1, root1);

        Category root2 = Category.createRoot("루트2", 2);
        Category child2 = CategoryFixture.child("자식2", 1, root2);
        ReflectionTestUtils.setField(root2, "status", CategoryStatus.ARCHIVED);

        // when & then
        assertThatThrownBy(child1::activate)
            .isInstanceOf(ServiceException.class)
            .hasMessage(CategoryErrorCode.PARENT_NOT_ACTIVE.getMessage());

        assertThatThrownBy(child2::activate)
            .isInstanceOf(ServiceException.class)
            .hasMessage(CategoryErrorCode.PARENT_NOT_ACTIVE.getMessage());
    }

    @Test
    @DisplayName("카테고리 비활성화 성공")
    void disableSuccess() {
        // given
        Category category = Category.createRoot("루트", 1);
        category.activate();

        // when
        category.disable();

        // then
        assertThat(category.getStatus()).isEqualTo(CategoryStatus.DISABLED);
    }
}