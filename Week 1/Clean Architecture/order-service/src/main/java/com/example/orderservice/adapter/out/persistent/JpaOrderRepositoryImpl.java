package com.example.orderservice.adapter.out.persistent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.example.orderservice.application.port.out.OrderRepository;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.model.OrderId;
import com.example.orderservice.domain.model.OrderItem;

@Repository
public class JpaOrderRepositoryImpl implements OrderRepository {
    final private JpaOrderRepository jpaOrderRepository;

    public JpaOrderRepositoryImpl(JpaOrderRepository jpaOrderRepository) {
        this.jpaOrderRepository = jpaOrderRepository;
    }

    @Override
    public void save(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId().getValue());
        entity.setTotal(order.calculateTotal());
        entity.setStatus(order.getStatus().toString());

        // Save items
        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> {
                    OrderItemEntity itemEntity = new OrderItemEntity();
                    itemEntity.setProductId(item.getProductId());
                    itemEntity.setQuantity(item.getQuantity());
                    itemEntity.setPrice(item.getPrice().getAmount());
                    return itemEntity;
                })
                .collect(Collectors.toList());
        entity.setItems(itemEntities);

        jpaOrderRepository.save(entity);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaOrderRepository.findById(orderId.getValue())
                .map(entity -> {
                    List<OrderItem> items = entity.getItems().stream()
                            .map(itemEntity -> new OrderItem(
                                    itemEntity.getProductId(),
                                    itemEntity.getQuantity(),
                                    itemEntity.getPrice()))
                            .collect(Collectors.toList());

                    return new Order(
                            new OrderId(entity.getId()),
                            items);
                });
    }

}
