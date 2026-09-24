package com.fittura.domain.delivery.facade;

import com.fittura.domain.delivery.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryFacade {

    private final DeliveryService deliveryService;
}
