package com.tanja.order_service.dao;

import com.tanja.order_service.rest.OrderResource;
import com.tanja.order_service.vao.Order;
import io.smallrye.mutiny.Uni;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class OrderRepository implements ReactivePanacheMongoRepository<Order> {

    private static final Logger LOG = LoggerFactory.getLogger(OrderRepository.class.getName());

    public Uni<Void> createOrder(String orderNumber, String userId, String productId, double totalCost ) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setTotalCost(totalCost);
        return persist(order).replaceWithVoid()
                .onFailure().invoke(error -> LOG.error("Failed to create order. Error: {}", error.getMessage()));
    }

    public Uni<Response> getOrderById(String id) {
        ObjectId objectId = new ObjectId(id);
        return findById(objectId)
                .onItem().ifNotNull().transform(order -> {
                    LOG.info("Order found by ID: {}", id);
                    return Response.ok(order).build();
                })
                .onItem().ifNull().continueWith(() -> {
                    LOG.warn("Order not found by ID: {}", id);
                    return Response.status(Response.Status.NOT_FOUND).build();
                })
                .onFailure().recoverWithItem(error -> {
                    LOG.error("Failed to get order by ID: {}", error.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    public Uni<Void> updateOrder(String id, String orderNumber, String userId, String productId, double totalCost) {
        ObjectId objectId = new ObjectId(id);
        return findById(objectId)
                .onItem().ifNotNull().transformToUni(order -> {
                    order.setOrderNumber(orderNumber);
                    order.setUserId(userId);
                    order.setProductId(productId);
                    order.setTotalCost(totalCost);
                    return order.update().replaceWithVoid();
                })
                .onFailure().invoke(error -> LOG.error("Failed to update order. Error: {}", error.getMessage()));
    }

    public Uni<Void> deleteOrder(String id) {
        ObjectId objectId = new ObjectId(id);
        return deleteById(objectId).replaceWithVoid()
                .onFailure().invoke(error -> LOG.error("Failed to delete order. Error: {}", error.getMessage()));
    }

    public Uni<List<Order>> getAllOrders() {
        return findAll().list()
                .onFailure().invoke(error -> LOG.error("Failed to get all orders. Error: {}", error.getMessage()));
    }
}
