package com.fittura.domain.product.categry.controller;

import com.fittura.domain.product.categry.entity.Category;
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
            .andExpect(jsonPath("$.code").value("C404-01"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 카테고리입니다."));
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
            .andExpect(jsonPath("$.code").value("C404-02"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 상위 카테고리입니다."));
    }


    // ===== 헬퍼 메서드 ====

    private Category getLatestCategory() {
        return categoryRepository.findFirstByOrderByIdDesc()
            .orElseThrow();
    }
}