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
import com.fittura.domain.product.sku.entity.Color;
import com.fittura.domain.product.sku.entity.Material;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.ColorRepository;
import com.fittura.domain.product.sku.repository.MaterialRepository;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 시드 (기존 ProductInitData + ProductBulkTestData 통합).
 * <p>
 * "케이스만" 보여주는 최소 데이터:
 * - COMPLETE(조립형)  : composition 2개 + 색/재질 다른 SKU  → 원형 식탁, 식탁 의자
 * - COMPONENT(부품)   : composition 없음, 부품 카테고리      → 상판/다리/좌판·등받이/손잡이 등
 * - COMPONENT(단품)   : composition 없음, 부품 아닌 완제품    → 3단 서랍장
 * - 상태 다양화: DISCONTINUED/DISABLED 각 1, SKU 단종 1, "ACTIVE인데 재고 0"(일시품절) 다수
 * - 페이징 확인용으로 "원형" 카테고리 하나만 20개 초과로 채움
 * <p>
 * color/material 은 아직 별도 생성 로직이 없어 여기서 레포지토리로 직접 마스터를 만든다.
 */
@Component
@Profile("dev")
@Order(3)
@RequiredArgsConstructor
public class ProductInitData implements ApplicationRunner {

    private static final List<String> COLOR_NAMES =
        List.of("오크", "월넛", "블랙", "실버", "베이지", "차콜");
    private static final List<String> MATERIAL_NAMES =
        List.of("원목", "스틸", "패브릭");
    private static final List<String> SIZE_LABELS = List.of("S", "M", "L", "XL");

    private static final int PAGING_PAD = 22; // "원형" 카테고리 페이징용

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ColorRepository colorRepository;
    private final MaterialRepository materialRepository;
    private final ProductFacade productFacade;

    private Map<String, Long> colorIds;
    private Map<String, Long> materialIds;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }

        // ===== 0. 색상/재질 마스터 (레포지토리 직접 생성) =====
        colorIds = new LinkedHashMap<>();
        for (String name : COLOR_NAMES) {
            colorIds.put(name, colorRepository.save(Color.create(name)).getId());
        }
        materialIds = new LinkedHashMap<>();
        for (String name : MATERIAL_NAMES) {
            materialIds.put(name, materialRepository.save(Material.create(name)).getId());
        }

        // ===== 1. 부품(COMPONENT) =====
        Long tableTopId = component(getCat("식탁 상판"), "원목 상판 800X800", "L",
            sku(89_000, 30, "오크", "원목"), sku(99_000, 20, "월넛", "원목"));
        Long tableLegId = component(getCat("식탁 다리"), "철제 테이블 다리 4개 세트", "L",
            sku(49_000, 40, "블랙", "스틸"), sku(52_000, 0, "실버", "스틸"));
        Long chairBodyId = component(getCat("의자 좌판"), "패브릭 좌판·등받이 일체형", "M",
            sku(59_000, 50, "베이지", "패브릭"), sku(59_000, 35, "차콜", "패브릭"));
        Long chairLegId = component(getCat("의자 다리"), "원목 의자 다리 프레임", null,
            sku(29_000, 60, "오크", "원목"));
        component(getCat("서랍장 손잡이"), "알루미늄 손잡이", "S",
            sku(4_000, 100, "실버", "스틸"));

        // ===== 2. 완제품(COMPLETE, 조립형) =====
        complete(getCat("원형"), "핏투라 원형 식탁 800", "L", DeliveryType.INSTALLATION,
            List.of(sku(139_000, 15, "오크", "원목"), sku(149_000, 10, "월넛", "원목")),
            List.of(comp(firstSkuId(tableTopId), 1, 0),
                comp(firstSkuId(tableLegId), 1, 1)));
        List<Long> padIds = pad(getCat("원형"), PAGING_PAD);

        complete(getCat("식탁 의자"), "핏투라 식탁 의자", "M", DeliveryType.PARCEL,
            List.of(sku(69_000, 40, "베이지", "패브릭")),
            List.of(comp(firstSkuId(chairBodyId), 1, 0),
                comp(firstSkuId(chairLegId), 1, 1)));

        // ===== 3. 단품(COMPONENT, 조립X 완제품) =====
        component(getCat("3단 서랍장"), "3단 원목 서랍장", "L",
            sku(119_000, 10, "오크", "원목"), sku(129_000, 0, "월넛", "원목"));

        // 전체 일시품절 케이스
        component(getCat("5단 서랍장"), "5단 수납 서랍장 (전체 품절)", "XL",
            sku(159_000, 0, "오크", "원목"), sku(169_000, 0, "월넛", "원목"));

        // ===== 4. 전체 활성화 =====
        productRepository.findAll().forEach(Product::activate);

        // ===== 5. 상태 다양화 =====
        productRepository.findById(padIds.get(0)).ifPresent(Product::discontinue); // 단종
        productRepository.findById(padIds.get(1)).ifPresent(Product::disable);     // 일시 숨김
        discontinueOneSku(padIds.get(2));                                          // SKU 단종
    }


    // ========== 상품 생성 ==========

    private Long component(Category category, String name, String sizeLabel, SkuCreateReqDto... skus) {
        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            name,
            name + " 테스트 설명입니다.",
            ProductType.COMPONENT,
            DeliveryType.PARCEL,
            10.0, 50.0, 50.0, 50.0,
            List.of(skus),
            sizeAttributes(sizeLabel),
            List.of()
        ));
    }

    /**
     * COMPLETE (조립형) — composition 필수
     */
    private Long complete(
        Category category, String name, String sizeLabel, DeliveryType deliveryType,
        List<SkuCreateReqDto> skus, List<CompositionCreateReqDto> compositions
    ) {
        return productFacade.createProduct(new ProductCreateReqDto(
            category.getId(),
            name,
            name + " 테스트 설명입니다.",
            ProductType.COMPLETE,
            deliveryType,
            20.0, 80.0, 72.0, 80.0,
            skus,
            sizeAttributes(sizeLabel),
            compositions
        ));
    }

    /**
     * 한 카테고리 페이징용 패딩 — COMPONENT(단품), 색/재질 다른 SKU 2개, 일부 재고 0/사이즈 없음
     */
    private List<Long> pad(Category category, int count) {
        List<Long> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String color1 = COLOR_NAMES.get(i % COLOR_NAMES.size());
            String color2 = COLOR_NAMES.get((i + 2) % COLOR_NAMES.size());
            String material = MATERIAL_NAMES.get(i % MATERIAL_NAMES.size());
            String sizeLabel = (i % 3 == 0) ? null : SIZE_LABELS.get(i % SIZE_LABELS.size());
            int stock = (i % 7 == 0) ? 0 : 10 + i; // 7의 배수는 재고 0(일시품절)
            long price = 90_000L + i * 1_000L;

            ids.add(component(category, "핏투라 원형 식탁 " + i, sizeLabel,
                sku(price, stock, color1, material),
                sku(price + 2_000L, 20, color2, material)));
        }
        return ids;
    }


    // ========== 헬퍼 ==========

    private SkuCreateReqDto sku(long price, int stockQuantity, String colorName, String materialName) {
        return new SkuCreateReqDto(price, stockQuantity, colorIds.get(colorName), materialIds.get(materialName));
    }

    private CompositionCreateReqDto comp(Long childSkuId, int quantity, int sortOrder) {
        return new CompositionCreateReqDto(childSkuId, quantity, sortOrder);
    }

    private List<AttributeCreateReqDto> sizeAttributes(String sizeLabel) {
        // 사이즈는 선택값 — 없으면 속성 자체를 넣지 않음
        return sizeLabel == null
            ? List.of()
            : List.of(new AttributeCreateReqDto(AttributeKey.SIZE_LABEL, sizeLabel));
    }

    private Category getCat(String name) {
        return categoryRepository.findAll().stream()
            .filter(category -> category.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "시드 카테고리를 찾을 수 없습니다: " + name + " (CategoryInitData 실행 여부 확인)"
            ));
    }

    /**
     * 조합 childSkuId 용 — 해당 부품의 첫 SKU id
     */
    private Long firstSkuId(Long productId) {
        return activeSkus(productId).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("시드 SKU를 찾을 수 없습니다: productId=" + productId))
            .getId();
    }

    private void discontinueOneSku(Long productId) {
        List<ProductSku> skus = activeSkus(productId);
        if (skus.size() > 1) {
            skus.get(0).discontinue(); // 여러 SKU 중 하나만 단종
        }
    }

    private List<ProductSku> activeSkus(Long productId) {
        return productSkuRepository.findByProductIdAndStatusNot(productId, SkuStatus.ARCHIVED);
    }
}
