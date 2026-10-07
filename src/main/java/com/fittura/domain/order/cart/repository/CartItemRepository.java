package com.fittura.domain.order.cart.repository;

import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemRepositoryCustom {

    Optional<CartItem> findByCartAndProductSku(Cart cart, ProductSku sku);

    @EntityGraph(attributePaths = "productSku")
    Optional<CartItem> findByIdAndCart_MemberId(Long itemId, Long memberId);

    @Modifying
    @Query("""
        DELETE FROM CartItem ci
        WHERE ci.id IN :cartItemIds
        AND ci.cart.memberId = :memberId
        """)
    void deleteCartItems(@Param("cartItemIds") Set<Long> cartItemIds, @Param("memberId") Long memberId);

    boolean existsByCart_IdAndProductSku_Id(Long cartId, Long productSkuId);
}
