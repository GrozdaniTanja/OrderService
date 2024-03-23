package com.tanja.order_service.rest;

import com.tanja.order_service.dao.OrderRepository;
import com.tanja.order_service.vao.Order;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/order")
public class OrderResource {


    private static final Logger LOG = LoggerFactory.getLogger(OrderRepository.class.getName());



    @Inject
    OrderRepository orderRepository;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> createOrder(Order order) {
        return orderRepository.createOrder(order.getOrderNumber(), order.getUserId(), order.getProductId(), order.getTotalCost())
                .replaceWithVoid()
                .onFailure().invoke(error -> LOG.error("Failed to create order: {}", error.getMessage()));
    }



    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getOrderById(@PathParam("id") String id) {
        return orderRepository.getOrderById(id);
    }


    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> updateOrder(@PathParam("id") String id, Order updatedOrder) {
        return orderRepository.updateOrder(id, updatedOrder.getOrderNumber(), updatedOrder.getUserId(), updatedOrder.getProductId(), updatedOrder.getTotalCost())
                .replaceWithVoid()
                .onFailure().invoke(error -> LOG.error("Failed to update order: {}", error.getMessage()));
    }


    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Void> deleteOrder(@PathParam("id") String id) {
        return orderRepository.deleteOrder(id);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAllOrders() {
        return orderRepository.getAllOrders()
                .onItem().transform(orders -> {
                    if (orders != null) {
                        return Response.ok(orders).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("No orders found").build();
                    }
                })
                .onFailure().recoverWithItem(error -> {
                    LOG.error("Failed to get all orders: {}", error.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to get all orders").build();
                });
    }




}
