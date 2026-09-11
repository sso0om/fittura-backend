package com.fittura.global.initdata;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.product.facade.ProductFacade;
import com.fittura.domain.product.product.constant.AttributeKey;
import com.fittura.domain.product.product.constant.DeliveryType;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.dto.request.AttributeCreateReqDto;
import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
@Order(4)
@RequiredArgsConstructor
public class ProductBulkTestData implements ApplicationRunner {

    private static final String BULK_NAME_PREFIX = "[TEST] ";
    private static final int PART_COUNT_PER_CATEGORY = 30;       // 부품(COMPONENT): 부품 카테고리당
    private static final int COMPOSED_COUNT_PER_CATEGORY = 20;   // 조합 완제품(COMPLETE): 카테고리당
    private static final int STANDALONE_COUNT_PER_CATEGORY = 20; // 단품(COMPONENT): 완제품 카테고리당

    private static final List<String> COLORS = List.of("오크", "월넛", "블랙", "화이트", "그레이");
    private static final List<String> MATERIALS = List.of("원목", "스틸", "패브릭", "인조가죽");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductFacade productFacade;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean alreadySeeded = productRepository.findAll().stream()
            .anyMatch(product -> product.getName().startsWith(BULK_NAME_PREFIX));
        if (alreadySeeded) {
            return;
        }

        List<Long> bulkIds = new ArrayList<>();

        // ===== 1. 부품(COMPONENT) — 부품 리프 카테고리 전부(8개) =====
        List<Category> partCategories = List.of(
            getCategory("식탁 상판"),
            getCategory("식탁 다리"),
            getCategory("의자 좌판"),
            getCategory("의자 등받이"),
            getCategory("의자 다리"),
            getCategory("서랍장 상판"),
            getCategory("서랍장 서랍"),
            getCategory("서랍장 손잡이")
        );
        for (Category category : partCategories) {
            for (int i = 1; i <= PART_COUNT_PER_CATEGORY; i++) {
                bulkIds.add(createPart(category, i));
            }
        }

        // ===== 2. 조합 완제품(COMPLETE + composition) =====
        // 조합용 부품 SKU는 기존 ProductInitData가 만들어둔 원본 상품에서 가져옴
        Long topSkuId = findSkuIdByProductName("원목 상판 800X800", "오크");
        Long tableLegSkuId = findSkuIdByProductName("철제 테이블 다리 4개 세트", "블랙");
        Long seatSkuId = findSkuIdByProductName("패브릭 좌판", "베이지");
        Long backSkuId = findSkuIdByProductName("원목 등받이", "오크");
        Long chairLegSkuId = findSkuIdByProductName("원목 의자 다리 프레임", "오크");

        // 테이블류(상판+다리): 원형, 바 테이블
        for (Category category : List.of(getCategory("원형"), getCategory("바 테이블"))) {
            for (int i = 1; i <= COMPOSED_COUNT_PER_CATEGORY; i++) {
                bulkIds.add(createComposedTable(category, i, topSkuId, tableLegSkuId));
            }
        }
        // 의자류(좌판+등받이+다리): 식탁 의자, 접이식 의자
        for (Category category : List.of(getCategory("식탁 의자"), getCategory("접이식 의자"))) {
            for (int i = 1; i <= COMPOSED_COUNT_PER_CATEGORY; i++) {
                bulkIds.add(createComposedChair(category, i, seatSkuId, backSkuId, chairLegSkuId));
            }
        }

        // ===== 3. 단품(COMPONENT, 구성품 없는 완제품) — 모든 완제품 카테고리 =====
        List<Category> finishedCategories = List.of(
            getCategory("원형"),
            getCategory("바 테이블"),
            getCategory("식탁 의자"),
            getCategory("접이식 의자"),
            getCategory("3단 서랍장"),
            getCategory("5단 서랍장")
        );
        for (Category category : finishedCategories) {
            for (int i = 1; i <= STANDALONE_COUNT_PER_CATEGORY; i++) {
                bulkIds.add(createStandaloneProduct(category, i));
            }
        }

        // ===== 4. 활성화 =====
        productRepository.findAllById(bulkIds).forEach(Product::activate);

        // ===== 5. 상태 다양화 (필터/오버레이 라벨 확인용) =====
        for (int i = 0; i < bulkIds.size(); i++) {
            Long productId = bulkIds.get(i);
            if (i % 25 == 0) {
                productFacade.discontinueProduct(productId);                  // 영구 품절(단종)
            } else if (i % 10 == 0) {
                // 일시 품절: 재고 0 → 파생 로직이 품절 처리
                productSkuRepository.findByProductIdAndStatusNot(productId, SkuStatus.ARCHIVED)
                    .forEach(sku -> sku.update(sku.getPrice(), 0, sku.getColor(), sku.getMaterial()));
            }
        }
    }


    // ========== 상품 생성 ==========

    /** 부품(COMPONENT) — 상판/다리 등 개별 부품 */
    private Long createPart(Category category, int index) {
        String color = COLORS.get(index % COLORS.size());
        String material = MATERIALS.get(index % MATERIALS.size());
        long price = 10_000L + (index * 1_000L);

        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            BULK_NAME_PREFIX + category.getName() + " " + index,
            "테스트용 더미 데이터입니다.",
            ProductType.COMPONENT,
            DeliveryType.PARCEL,
            5.0, 40.0, 10.0, 40.0,
            List.of(new SkuCreateReqDto(price, 20, color, material)),
            List.of(new AttributeCreateReqDto(AttributeKey.SIZE_LABEL, "테스트 규격 " + index)),
            List.of()
        ));
    }

    /** 단품(COMPONENT) — 구성품 없이 자체 SKU로 파는 완제품 */
    private Long createStandaloneProduct(Category category, int index) {
        String color = COLORS.get(index % COLORS.size());
        String material = MATERIALS.get(index % MATERIALS.size());
        long price = 80_000L + (index * 1_500L);

        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            BULK_NAME_PREFIX + category.getName() + " 단품 " + index,
            "테스트용 더미 데이터입니다.",
            ProductType.COMPONENT,          // ← 단품은 COMPONENT (구성품 없음)
            DeliveryType.PARCEL,
            10.0, 60.0, 50.0, 60.0,
            List.of(new SkuCreateReqDto(price, 20, color, material)),
            List.of(new AttributeCreateReqDto(AttributeKey.SIZE_LABEL, "테스트 규격 " + index)),
            List.of()                        // 구성품 없음
        ));
    }

    /** 조합 완제품(COMPLETE) — 상판+다리 */
    private Long createComposedTable(Category category, int index, Long topSkuId, Long legSkuId) {
        long price = 120_000L + (index * 2_000L);

        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            BULK_NAME_PREFIX + category.getName() + " " + index,
            "테스트용 더미 데이터입니다.",
            ProductType.COMPLETE,
            DeliveryType.INSTALLATION,
            20.0, 80.0, 72.0, 80.0,
            List.of(new SkuCreateReqDto(price, 10, "오크", "원목")),
            List.of(new AttributeCreateReqDto(AttributeKey.SIZE_LABEL, "800X800")),
            List.of(
                new CompositionCreateReqDto(topSkuId, 1, 0),
                new CompositionCreateReqDto(legSkuId, 1, 1)
            )
        ));
    }

    /** 조합 완제품(COMPLETE) — 좌판+등받이+다리 */
    private Long createComposedChair(
        Category category, int index, Long seatSkuId, Long backSkuId, Long legSkuId
    ) {
        long price = 60_000L + (index * 1_500L);

        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            BULK_NAME_PREFIX + category.getName() + " " + index,
            "테스트용 더미 데이터입니다.",
            ProductType.COMPLETE,
            DeliveryType.PARCEL,
            7.5, 45.0, 85.0, 50.0,
            List.of(new SkuCreateReqDto(price, 15, "베이지", "패브릭")),
            List.of(new AttributeCreateReqDto(AttributeKey.SIZE_LABEL, "450X500")),
            List.of(
                new CompositionCreateReqDto(seatSkuId, 1, 0),
                new CompositionCreateReqDto(backSkuId, 1, 1),
                new CompositionCreateReqDto(legSkuId, 1, 2)
            )
        ));
    }


    // ========== 헬퍼 메서드 ==========

    private Category getCategory(String name) {
        return categoryRepository.findAll().stream()
            .filter(category -> category.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "시드 카테고리를 찾을 수 없습니다: " + name + " (CategoryInitData 실행 여부 확인)"
            ));
    }

    private Long findSkuIdByProductName(String productName, String color) {
        Product product = productRepository.findAll().stream()
            .filter(p -> p.getName().equals(productName))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "조합용 원본 부품 상품을 찾을 수 없습니다: " + productName + " (ProductInitData 실행 여부 확인)"
            ));

        return productSkuRepository.findByProductIdAndStatusNot(product.getId(), SkuStatus.ARCHIVED)
            .stream()
            .filter(sku -> color.equals(sku.getColor()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "조합용 SKU를 찾을 수 없습니다: " + productName + " / " + color
            ))
            .getId();
    }
}