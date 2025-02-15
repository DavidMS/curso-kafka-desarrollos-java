package com.imagina.kafka.command.action;

import com.imagina.kafka.api.request.OrderRequest;
import com.imagina.kafka.broker.message.OrderMessage;
import com.imagina.kafka.broker.producer.OrderProducer;
import com.imagina.kafka.entity.Order;
import com.imagina.kafka.entity.OrderItem;
import com.imagina.kafka.repository.OrderItemRepository;
import com.imagina.kafka.repository.OrderRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class OrderAction {

    @Autowired
    private OrderProducer orderProducer;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public Order convertToOrder(OrderRequest request) {
        var order = new Order();

        order.setOrderLocation(request.getOrderLocation());
        order.setCreditCardNumber(request.getCreditCardNumber());
        order.setOrderDateTime(OffsetDateTime.now());
        order.setOrderNumber(RandomStringUtils.randomAlphabetic(8).toUpperCase());

        var orderItems = request.getItems().stream().map(
                item -> {
                    var orderItem = new OrderItem();

                    orderItem.setItemName(item.getItemName());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setPrice(item.getPrice());
                    orderItem.setOrder(order);

                    return orderItem;
                }).toList();

        order.setOrderItems(orderItems);

        return order;
    }

    public void saveToDatabase(Order orderEntity) {
        orderRepository.save(orderEntity);
        orderEntity.getOrderItems().forEach(orderItemRepository::save);
    }

    public OrderMessage convertToOrderMessage(OrderItem item) {
        var orderMessage = new OrderMessage();

        orderMessage.setItemName(item.getItemName());
        orderMessage.setPrice(item.getPrice());
        orderMessage.setQuantity(item.getQuantity());
        orderMessage.setOrderNumber(item.getOrder().getOrderNumber());
        orderMessage.setOrderDateTime(item.getOrder().getOrderDateTime());
        orderMessage.setCreditCardNumber(item.getOrder().getCreditCardNumber());
        orderMessage.setOrderLocation(item.getOrder().getOrderLocation());

        return orderMessage;
    }

    public void sendToKafka(OrderMessage orderMessage) {
        orderProducer.sendOrder(orderMessage);
    }

}