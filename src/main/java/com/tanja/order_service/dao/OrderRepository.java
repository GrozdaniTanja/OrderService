package com.tanja.order_service.dao;

import com.tanja.order_service.jms.JMSContextManager;
import com.tanja.order_service.jms.Producer.OrderMessageProducer;
import com.tanja.order_service.vao.Order;
import io.smallrye.mutiny.Uni;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class OrderRepository implements ReactivePanacheMongoRepository<Order> {

    private static final Logger LOG = LoggerFactory.getLogger(OrderRepository.class.getName());

    @Inject
    OrderMessageProducer orderMessageProducer;

    @Inject
    JMSContextManager jmsContextManager;

    public Uni<Void> createOrder(String orderNumber, String userId, String productId, double totalCost) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setTotalCost(totalCost);
        return persist(order).replaceWithVoid().onItem().transform(ignored -> {
            String logMessage = "New order added: " + orderNumber;
            try (JMSContext context = jmsContextManager.getContext()) {
                Queue queue = context.createQueue("OrderServiceQueue");
                context.createProducer().send(queue, logMessage);
                LOG.info(logMessage);
            } catch (Exception e) {
                LOG.error("Failed to send log message", e);
            }
            return null;
        }).onFailure().invoke(throwable -> LOG.error("Failed to add group class", throwable)).replaceWithVoid();
    }

    public Uni<Response> getOrderById(String id) {
        ObjectId objectId = new ObjectId(id);
        return findById(objectId)
                .onItem().ifNotNull().transformToUni(order -> {
                    String logMessage = "Order found by ID" + id;
                    try (JMSContext context = jmsContextManager.getContext()) {
                        Queue queue = context.createQueue("OrderServiceQueue");
                        context.createProducer().send(queue, logMessage);
                        LOG.info(logMessage);
                    } catch (Exception e) {
                        LOG.error("Failed to send log message", e);
                    }
                    if (order != null) {
                        return Uni.createFrom().item(Response.ok(order).build());
                    } else {
                        return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).entity("Group class not found with id: " + id).build());
                    }
                }).onFailure().recoverWithItem(throwable -> {
                    LOG.error("Failed to get group class by id: " + id, throwable);
                    return Response.serverError().entity("Failed to retrieve group class").build();
                });
    }

    public Uni<Void> updateOrder(String id, String orderNumber, String userId, String productId, double totalCost) {
        try {
            ObjectId objectId = new ObjectId(id);
            return findById(objectId)
                    .onItem().ifNotNull().transformToUni(order -> {
                        order.setOrderNumber(orderNumber);
                        order.setUserId(userId);
                        order.setProductId(productId);
                        order.setTotalCost(totalCost);
                        return order.update().replaceWithVoid()
                                .onItem().invoke(ignored -> {
                                    String logMessage = "Updated order with id: " + id;
                                    try (JMSContext context = jmsContextManager.getContext()) {
                                        Queue queue = context.createQueue("OrderServiceQueue");
                                        context.createProducer().send(queue, logMessage);
                                        LOG.info(logMessage);
                                    } catch (Exception e) {
                                        LOG.error("Failed to send log message", e);
                                    }
                                });
                    }).onFailure().recoverWithNull();
        } catch (Exception ex) {
            LOG.error("Invalid ObjectId format for id: " + id, ex);
            return Uni.createFrom().failure(ex);
        }
    }

    public Uni<Void> deleteOrder(String id) {
        ObjectId objectId = new ObjectId(id);
        return deleteById(objectId).replaceWithVoid()
                .onItem().invoke(ignored -> {
                    String logMessage = "Deleted order with id: " + id;
                    try (JMSContext context = jmsContextManager.getContext()) {
                        Queue queue = context.createQueue("OrderServiceQueue");
                        context.createProducer().send(queue, logMessage);
                        LOG.info(logMessage);
                    } catch (Exception e) {
                        LOG.error("Failed to send log message", e);
                    }
                })
                .onFailure().invoke(throwable -> LOG.error("Failed to delete order with id: " + id, throwable));
        }

    public Uni<List<Order>> getAllOrders() {
        return findAll().list()
                .onItem().transform(orders -> {
                    String logMessage = "Retrieved all orders";
                    try (JMSContext context = jmsContextManager.getContext()) {
                        Queue queue = context.createQueue("OrderServiceQueue");
                        context.createProducer().send(queue, logMessage);
                        LOG.info(logMessage);
                    } catch (Exception e) {
                        LOG.error("Failed to send log message", e);
                    }
                    return orders;
                })
                .onFailure().invoke(error -> LOG.error("Failed to get all orders. Error: {}", error.getMessage()));
    }

}
