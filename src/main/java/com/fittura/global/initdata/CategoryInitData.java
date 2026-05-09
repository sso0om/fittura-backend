package com.fittura.global.initdata;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class CategoryInitData implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        // ===== 루트 =====
        Category table = createRoot("식탁", 1);
        Category chair = createRoot("의자", 2);
        Category drawer = createRoot("서랍장", 3);


        // ===== 하위 =====
        Category normalTable = createChild("일반 식탁", 1, table);
        createChild("바 테이블", 2, table);
        createChild("원형", 1, normalTable);

        createChild("식탁 의자", 1, chair);
        createChild("접이식 의자", 2, chair);

        createChild("3단 서랍장", 1, drawer);
        createChild("5단 서랍장", 2, drawer);
    }

    private Category createRoot(String name, int order) {
        Category c = Category.createRoot(name, order);
        c.activate();
        return categoryRepository.save(c);
    }

    private Category createChild(String name, int order, Category parent) {
        Category c = Category.createChild(name, order, parent);
        c.activate();
        return categoryRepository.save(c);
    }
}
