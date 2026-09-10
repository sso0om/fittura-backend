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
import com.fittura.domain.product.sku.entity.ProductSku;
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
    private static final int COMPONENT_COUNT_PER_CATEGORY = 15; // 부품 카테고리당 생성 개수
    private static final int COMPLETE_COUNT = 30;               // 완제품 총 생성 개수

    private static final List<String> COLORS = List.of("오크", "월넛", "블랙", "화이트", "그레이");
    private static final List<String> MATERIALS = List.of("원목", "스틸", "패브릭", "인조가죽");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductFacade productFacade;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 이미 벌크 테스트 데이터가 있으면 재실행하지 않음 (앱 재시작마다 중복 생성 방지)
        boolean alreadySeeded = productRepository.findAll().stream()
            .anyMatch(product -> product.getName().startsWith(BULK_NAME_PREFIX));
        if (alreadySeeded) {
            return;
        }

        // ===== 1. 부품(COMPONENT) 대량 생성 =====
        // ProductInitData와 동일한 카테고리 이름 사용 (CategoryInitData 실행 전제)
        List<Category> componentCategories = List.of(
            getCategory("식탁 상판"),
            getCategory("식탁 다리"),
            getCategory("의자 좌판"),
            getCategory("의자 등받이"),
            getCategory("의자 다리")
        );

        List<Long> bulkIds = new ArrayList<>();
        for (Category category : componentCategories) {
            for (int i = 1; i <= COMPONENT_COUNT_PER_CATEGORY; i++) {
                bulkIds.add(createBulkComponent(category, i));
            }
        }

        // ===== 2. 완제품(COMPLETE) 대량 생성 =====
        // 조합용 부품 SKU는 기존 ProductInitData가 만들어둔 원본 상품에서 그대로 가져옴
        Long topSkuId = findSkuIdByProductName("원목 상판 800X800", "오크");
        Long tableLegSkuId = findSkuIdByProductName("철제 테이블 다리 4개 세트", "블랙");
        Long seatSkuId = findSkuIdByProductName("패브릭 좌판", "베이지");
        Long backSkuId = findSkuIdByProductName("원목 등받이", "오크");
        Long chairLegSkuId = findSkuIdByProductName("원목 의자 다리 프레임", "오크");

        Category roundTableCategory = getCategory("원형");
        Category diningChairCategory = getCategory("식탁 의자");

        for (int i = 1; i <= COMPLETE_COUNT; i++) {
            if (i % 2 == 0) {
                bulkIds.add(createBulkTable(roundTableCategory, i, topSkuId, tableLegSkuId));
            } else {
                bulkIds.add(createBulkChair(diningChairCategory, i, seatSkuId, backSkuId, chairLegSkuId));
            }
        }

        // ===== 3. 활성화 =====
        productRepository.findAllById(bulkIds).forEach(Product::activate);

        // ===== 4. 상태 다양화 (목록 필터/정렬/오버레이 라벨 확인용) =====
        for (int i = 0; i < bulkIds.size(); i++) {
            Long productId = bulkIds.get(i);

            if (i % 25 == 0) {
                // 25개마다 하나씩 영구 품절(ProductStatus.DISCONTINUED)
                productFacade.discontinueProduct(productId);
            } else if (i % 10 == 0) {
                // 10개마다 하나씩 임시 품절(SkuStatus.SOLDOUT)
                productSkuRepository.findByProductIdAndStatusNot(productId, SkuStatus.ARCHIVED)
                    .forEach(ProductSku::soldOut);
            }
        }
    }


    // ========== 상품 생성 ==========

    private Long createBulkComponent(Category category, int index) {
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

    private Long createBulkTable(Category category, int index, Long topSkuId, Long legSkuId) {
        long price = 120_000L + (index * 2_000L);

        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            BULK_NAME_PREFIX + "원형 식탁 " + index,
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

    private Long createBulkChair(
        Category category, int index, Long seatSkuId, Long backSkuId, Long legSkuId
    ) {
        long price = 60_000L + (index * 1_500L);

        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            BULK_NAME_PREFIX + "식탁 의자 " + index,
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
        return categoryRepository.findAll()
            .stream()
            .filter(category -> category.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "시드 카테고리를 찾을 수 없습니다: " + name + " (CategoryInitData 실행 여부 확인)"
            ));
    }

    private Long findSkuIdByProductName(String productName, String color) {
        Product product = productRepository.findAll()
            .stream()
            .filter(p -> p.getName().equals(productName))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "조합용 원본 부품 상품을 찾을 수 없습니다: " + productName
                    + " (기존 ProductInitData 실행 여부 확인)"
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