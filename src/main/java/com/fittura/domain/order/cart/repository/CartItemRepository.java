package com.fittura.domain.order.cart.repository;

import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long>, CartItemRepositoryCustom {

    Optional<CartItem> findByCartAndProductSku(Cart cart, ProductSku sku);

    Optional<CartItem> findByIdAndCart_MemberId(Long itemId, Long memberId);

    @Modifying
    @Query("""
        DELETE FROM CartItem ci
        WHERE ci.cart.memberId = :memberId
        AND ci.productSku.id IN :skuIds
        """)
    void deleteByMemberIdAndSkuIds(@Param("memberId") Long memberId, @Param("skuIds") Set<Long> skuIds);
}
