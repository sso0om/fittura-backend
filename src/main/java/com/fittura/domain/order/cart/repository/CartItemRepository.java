package com.fittura.domain.order.cart.repository;

import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long>, CartItemRepositoryCustom {
    Optional<CartItem> findByCartAndProductSku(Cart cart, ProductSku sku);

    Optional<CartItem> findByIdAndCart_MemberId(Long itemId, Long memberId);
}
