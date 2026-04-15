package com.fittura.domain.product.categry.controller;

import com.fittura.domain.product.categry.constant.CategoryStatus;
import com.fittura.domain.product.categry.entity.Category;
import com.fittura.domain.product.categry.error.CategoryErrorCode;
import com.fittura.domain.product.categry.repository.CategoryRepository;
import com.fittura.domain.product.categry.support.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminCategoryControllerV1Test {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final String CATEGORY_URL = "/api/admin/v1/categories";


    // ========== 카테고리 전체 조회 ==========

    @Test
    @DisplayName("카테고리 전체 조회 성공")
    void getAllCategoriesSuccess() throws Exception {
        Category root1 = categoryRepository.save(CategoryFixture.rootActive());
        Category root2 = categoryRepository.save(CategoryFixture.root("최상위 카테고리2", 1));
        Category child1 = categoryRepository.save(CategoryFixture.child("자식 카테고리1", 0, root1));
        Category child2 = categoryRepository.save(CategoryFixture.child("자식 카테고리2", 1, root1));
        Category child3 = categoryRepository.save(CategoryFixture.child("자식 카테고리2-1", 0, child2));

        // given
        ResultActions resultActions = mockMvc
            .perform(get(CATEGORY_URL))
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("getAllCategories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.data.length()").value(2)) // 루트 개수
            .andExpect(jsonPath("$.data[0].id").value(root1.getId()))
            .andExpect(jsonPath("$.data[0].children.length()").value(2))
            .andExpect(jsonPath("$.data[0].children[1].children.length()").value(1)) // 손자 확인
            .andExpect(jsonPath("$.data[1].id").value(root2.getId()))
            .andExpect(jsonPath("$.data[1].children.length()").value(0))
        ;
    }


    // ========== 카테고리 단건 조회 ==========

    @Test
    @DisplayName("카테고리 조회 성공")
    void getCategorySuccess() throws Exception {
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        // given
        ResultActions resultActions = mockMvc
            .perform(get(CATEGORY_URL + "/" + category.getId()))
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("getCategory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.data.id").value(category.getId()))
            .andExpect(jsonPath("$.data.name").value(category.getName()))
            .andExpect(jsonPath("$.data.parentId").isEmpty())
            .andExpect(jsonPath("$.data.depth").value(category.getDepth()))
            .andExpect(jsonPath("$.data.sortOrder").value(category.getSortOrder()));
    }

    @Test
    @DisplayName("카테고리 조회 실패 - 잘못된 id")
    void getCategory() throws Exception {
        // given
        ResultActions resultActions = mockMvc
            .perform(get(CATEGORY_URL + "/999"))
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("getCategory"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CategoryErrorCode.NOT_FOUND_CATEGORY.getCode()))
            .andExpect(jsonPath("$.message").value(CategoryErrorCode.NOT_FOUND_CATEGORY.getMessage()));
    }


    // ========== 카테고리 생성 ==========

    @Test
    @DisplayName("루트 카테고리 생성 성공")
    void createRootCategorySuccess() throws Exception {
        // given
        String reqBody = """
            {
                "name" : "카테고리1",
                "sortOrder" : 1
            }
            """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(post(CATEGORY_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("createCategory"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("카테고리가 생성되었습니다."));

        Category category = getLatestCategory();

        resultActions
            .andExpect(jsonPath("$.data.id").value(category.getId()))
            .andExpect(jsonPath("$.data.name").value(category.getName()))
            .andExpect(jsonPath("$.data.parentId").isEmpty())
            .andExpect(jsonPath("$.data.depth").value(category.getDepth()))
            .andExpect(jsonPath("$.data.sortOrder").value(category.getSortOrder()));
    }

    @Test
    @DisplayName("자식 카테고리 생성 성공")
    void createChildCategorySuccess() throws Exception {
        Category parent = categoryRepository.save(CategoryFixture.rootActive());

        // given
        String reqBody = """
            {
                "name" : "하위 카테고리1",
                "parentId" : %d,
                "sortOrder" : 1
            }
        """.formatted(parent.getId());

        // when & then
        ResultActions resultActions = mockMvc
            .perform(post(CATEGORY_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("createCategory"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("카테고리가 생성되었습니다."));

        Category category = getLatestCategory();
        assertThat(category.getParent()).isNotNull();
        assertThat(category.getDepth()).isEqualTo(1);
        assertThat(category.getParent().getId()).isEqualTo(parent.getId());

        resultActions
            .andExpect(jsonPath("$.data.id").value(category.getId()))
            .andExpect(jsonPath("$.data.name").value(category.getName()))
            .andExpect(jsonPath("$.data.parentId").value(parent.getId()))
            .andExpect(jsonPath("$.data.depth").value(category.getDepth()))
            .andExpect(jsonPath("$.data.sortOrder").value(category.getSortOrder()));
    }

    @Test
    @DisplayName("자식 카테고리 생성 실패 - 부모가 없는 경우")
    void createChildCategoryFail() throws Exception {
        // given
        String reqBody = """
            {
                "name" : "하위 카테고리1",
                "parentId" : 999,
                "sortOrder" : 1
            }
        """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(post(CATEGORY_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("createCategory"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CategoryErrorCode.NOT_FOUND_PARENT_CATEGORY.getCode()))
            .andExpect(jsonPath("$.message").value(CategoryErrorCode.NOT_FOUND_PARENT_CATEGORY.getMessage()));
    }


    // ========== 카테고리 단건 수정 ==========
    @Test
    @DisplayName("카테고리 수정 성공 - 기본 정보 수정")
    void updateCategoryInfoSuccess() throws Exception {
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        // given
        String reqBody = """
            {
                "name" : "카테고리 변경",
                "sortOrder" : 2,
                "status" : "ACTIVE"
            }
        """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(put(CATEGORY_URL + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("updateCategory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("카테고리가 수정되었습니다."));

        Category after = getCategoryById(category.getId());

        assertThat(after.getName()).isEqualTo("카테고리 변경");
        assertThat(after.getParent()).isNull();
        assertThat(after.getDepth()).isEqualTo(0);
        assertThat(after.getSortOrder()).isEqualTo(2);
        assertThat(after.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
    }

    @Test
    @DisplayName("카테고리 수정 성공 - 부모 변경")
    void updateCategoryParentSuccess() throws Exception {
        Category root1 = categoryRepository.save(CategoryFixture.root("루트1", 1));
        Category root2 = categoryRepository.save(CategoryFixture.root("루트2", 2));
        Category category = categoryRepository.save(CategoryFixture.child("자식", 1, root1));

        // given
        String reqBody = """
            {
                "name" : "자식 변경",
                "parentId" : %d,
                "sortOrder" : 3,
                "status" : "ACTIVE"
            }
        """.formatted(root2.getId());

        // when & then
        ResultActions resultActions = mockMvc
            .perform(put(CATEGORY_URL + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("updateCategory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("카테고리가 수정되었습니다."));

        Category after = getCategoryById(category.getId());

        assertThat(after.getName()).isEqualTo("자식 변경");
        assertThat(after.getSortOrder()).isEqualTo(3);
        assertThat(after.getParent()).isEqualTo(root2);
        assertThat(after.getDepth()).isEqualTo(root2.getDepth() + 1);
        assertThat(after.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
    }

    @Test
    @DisplayName("카테고리 수정 성공 - 루트 카테고리로 변경")
    void updateCategoryToRootSuccess() throws Exception {
        Category root = categoryRepository.save(CategoryFixture.root("루트1", 1));
        Category category = categoryRepository.save(CategoryFixture.child("상위 카테고리", 1, root));
        Category child = categoryRepository.save(CategoryFixture.child("자식2", 1, category));

        // given
        String reqBody = """
            {
                "name" : "최상위 카테고리",
                "sortOrder" : 1,
                "status" : "ACTIVE"
            }
        """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(put(CATEGORY_URL + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("updateCategory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("카테고리가 수정되었습니다."));

        Category after = getCategoryById(category.getId());

        assertThat(after.getName()).isEqualTo("최상위 카테고리");
        assertThat(after.getSortOrder()).isEqualTo(1);
        assertThat(after.getParent()).isNull();
        assertThat(after.getDepth()).isEqualTo(0);
        assertThat(after.getStatus()).isEqualTo(CategoryStatus.ACTIVE);

        Category afterChild = getCategoryById(child.getId());

        assertThat(afterChild.getParent()).isEqualTo(after);
        assertThat(afterChild.getDepth()).isEqualTo(after.getDepth() + 1);
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않은 카테고리 수정")
    void updateCategoryNotFound() throws Exception {
        // given
        String reqBody = """
            {
                "name" : "카테고리 변경",
                "sortOrder" : 1,
                "status" : "ACTIVE"
            }
        """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(put(CATEGORY_URL + "/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("updateCategory"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CategoryErrorCode.NOT_FOUND_CATEGORY.getCode()))
            .andExpect(jsonPath("$.message").value(CategoryErrorCode.NOT_FOUND_CATEGORY.getMessage()));
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않은 부모 카테고리로 변경")
    void updateCategoryParentNotFound() throws Exception {
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        // given
        String reqBody = """
            {
                "name" : "카테고리 변경",
                "parentId" : 9999,
                "sortOrder" : 1,
                "status" : "ACTIVE"
            }
        """;

        // when & then
        ResultActions resultActions = mockMvc
            .perform(put(CATEGORY_URL + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("updateCategory"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CategoryErrorCode.NOT_FOUND_PARENT_CATEGORY.getCode()))
            .andExpect(jsonPath("$.message").value(CategoryErrorCode.NOT_FOUND_PARENT_CATEGORY.getMessage()));
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 자기 자신을 부모로 변경")
    void updateCategoryNotSelfParent() throws Exception {
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        // given
        String reqBody = """
            {
                "name" : "카테고리 변경",
                "parentId" : %d,
                "sortOrder" : 1,
                "status" : "ACTIVE"
            }
        """.formatted(category.getId());

        // when & then
        ResultActions resultActions = mockMvc
            .perform(put(CATEGORY_URL + "/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // verify
        resultActions
            .andExpect(handler().handlerType(AdminCategoryControllerV1.class))
            .andExpect(handler().methodName("updateCategory"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(CategoryErrorCode.NOT_SELF_PARENT.getCode()))
            .andExpect(jsonPath("$.message").value(CategoryErrorCode.NOT_SELF_PARENT.getMessage()));
    }


    // ===== 헬퍼 메서드 ====

    private Category getCategoryById(Long id) throws Exception {
        return categoryRepository.findById(id)
            .orElseThrow();
    }

    private Category getLatestCategory() {
        return categoryRepository.findFirstByOrderByIdDesc()
            .orElseThrow();
    }
}