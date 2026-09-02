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

import java.util.List;

@Component
@Profile("dev")
@Order(3)
@RequiredArgsConstructor
public class ProductInitData implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductFacade productFacade;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }

        // ===== 부품 카테고리 =====
        Category tableTopCategory = getCategory("식탁 상판");
        Category tableLegCategory = getCategory("식탁 다리");
        Category chairSeatCategory = getCategory("의자 좌판");
        Category chairBackCategory = getCategory("의자 등받이");
        Category chairLegCategory = getCategory("의자 다리");

        // ===== 완제품 카테고리 =====
        Category roundTableCategory = getCategory("원형");
        Category diningChairCategory = getCategory("식탁 의자");
        Category foldingChairCategory = getCategory("접이식 의자");


        // ===== 1. 부품(COMPONENT) =====
        Long woodTopId = createComponent(
            tableTopCategory, "원목 상판 800X800", "식탁용 원목 상판입니다. 다리와 조합해 사용하세요.",
            "800X800", 12.0, 80.0, 4.0, 80.0,
            List.of(
                new SkuCreateReqDto(89_000L, 30, "오크", "원목"),
                new SkuCreateReqDto(99_000L, 20, "월넛", "원목")
            )
        );

        Long tableLegId = createComponent(
            tableLegCategory, "철제 테이블 다리 4개 세트", "높이 720mm 철제 테이블 다리 4개 세트입니다.",
            "H720", 8.0, 8.0, 72.0, 8.0,
            List.of(
                new SkuCreateReqDto(49_000L, 40, "블랙", "스틸"),
                new SkuCreateReqDto(52_000L, 25, "실버", "스틸")
            )
        );

        Long seatId = createComponent(
            chairSeatCategory, "패브릭 좌판", "의자용 패브릭 좌판입니다.",
            "450X450", 3.5, 45.0, 8.0, 45.0,
            List.of(
                new SkuCreateReqDto(39_000L, 50, "베이지", "패브릭"),
                new SkuCreateReqDto(39_000L, 35, "차콜", "패브릭")
            )
        );

        Long chairBackId = createComponent(
            chairBackCategory, "원목 등받이", "좌판과 조합하는 원목 등받이입니다.",
            "450X400", 2.5, 45.0, 40.0, 5.0,
            List.of(
                new SkuCreateReqDto(25_000L, 45, "오크", "원목")
            )
        );

        Long chairLegId = createComponent(
            chairLegCategory, "원목 의자 다리 프레임", "좌판과 조합하는 원목 의자 다리 프레임입니다.",
            "H450", 4.0, 45.0, 45.0, 45.0,
            List.of(
                new SkuCreateReqDto(29_000L, 60, "오크", "원목")
            )
        );


        // ===== 2. 완제품(COMPLETE) =====
        Long roundTableId = createComplete(
            roundTableCategory, "핏투라 원형 식탁 800", "원목 상판과 철제 다리로 구성된 2인용 원형 식탁입니다.",
            "800X800", DeliveryType.INSTALLATION, 20.0, 80.0, 72.0, 80.0,
            List.of(
                new SkuCreateReqDto(139_000L, 15, "오크", "원목"),
                new SkuCreateReqDto(149_000L, 10, "월넛", "원목")
            ),
            List.of(
                new CompositionCreateReqDto(skuId(woodTopId, "오크"), 1, 0),
                new CompositionCreateReqDto(skuId(tableLegId, "블랙"), 1, 1)
            )
        );

        Long diningChairId = createComplete(
            diningChairCategory, "핏투라 식탁 의자", "패브릭 좌판과 원목 프레임으로 구성된 식탁 의자입니다.",
            "450X500", DeliveryType.PARCEL, 7.5, 45.0, 85.0, 50.0,
            List.of(
                new SkuCreateReqDto(69_000L, 40, "베이지", "패브릭"),
                new SkuCreateReqDto(69_000L, 30, "차콜", "패브릭")
            ),
            List.of(
                new CompositionCreateReqDto(skuId(seatId, "베이지"), 1, 0),
                new CompositionCreateReqDto(skuId(chairBackId, "오크"), 1, 1),
                new CompositionCreateReqDto(skuId(chairLegId, "오크"), 1, 2)
            )
        );

        Long foldingChairId = createComplete(
            foldingChairCategory, "핏투라 접이식 의자", "보관이 쉬운 접이식 의자입니다.",
            "420X480", DeliveryType.PARCEL, 5.0, 42.0, 80.0, 48.0,
            List.of(
                new SkuCreateReqDto(49_000L, 25, "차콜", "패브릭")
            ),
            List.of(
                new CompositionCreateReqDto(skuId(seatId, "차콜"), 1, 0),
                new CompositionCreateReqDto(skuId(chairLegId, "오크"), 1, 1)
            )
        );


        // ===== 3. 활성화 처리 =====
        productRepository.findAllById(
            List.of(woodTopId, tableLegId, seatId, chairBackId, chairLegId,
                roundTableId, diningChairId, foldingChairId)
        ).forEach(Product::activate);

        // 품절 UI 확인용
        findSku(foldingChairId, "차콜").soldOut();
    }


    // ========== 상품 생성 ==========

    private Long createComponent(
        Category category, String name, String description, String sizeLabel,
        double weight, double width, double height, double depth,
        List<SkuCreateReqDto> skus
    ) {
        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            name,
            description,
            ProductType.COMPONENT,
            DeliveryType.PARCEL,
            weight,
            width,
            height,
            depth,
            skus,
            List.of(new AttributeCreateReqDto(AttributeKey.SIZE_LABEL, sizeLabel)),
            List.of()
        ));
    }

    private Long createComplete(
        Category category, String name, String description, String sizeLabel,
        DeliveryType deliveryType,
        double weight, double width, double height, double depth,
        List<SkuCreateReqDto> skus,
        List<CompositionCreateReqDto> compositions
    ) {
        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            name,
            description,
            ProductType.COMPLETE,
            deliveryType,
            weight,
            width,
            height,
            depth,
            skus,
            List.of(new AttributeCreateReqDto(AttributeKey.SIZE_LABEL, sizeLabel)),
            compositions
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

    private ProductSku findSku(Long productId, String color) {
        return productSkuRepository.findByProductIdAndStatusNot(productId, SkuStatus.ARCHIVED)
            .stream()
            .filter(sku -> color.equals(sku.getColor()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "시드 SKU를 찾을 수 없습니다: productId=" + productId + ", color=" + color
            ));
    }

    private Long skuId(Long productId, String color) {
        return findSku(productId, color).getId();
    }
}