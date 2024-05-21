package com.tanja.order_service;

import com.tanja.order_service.dao.OrderRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class OrderResourceTest {

    @InjectMock
    OrderRepository orderRepository;

    @Test
    void testCreateOrder() {
        Mockito.when(orderRepository.createOrder(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble())).thenReturn(Uni.createFrom().nullItem());


        given()
                .contentType(ContentType.JSON)
                .body("{\"orderNumber\": \"Test OrderNumber\", \"userId\": \"Test UserId\", \"productId\": \"Test ProductId\", \"totalCost\": 30.5}")
                .when()
                .post("/order")
                .then()
                .statusCode(201);

        Mockito.verify(orderRepository).createOrder(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble());
    }

    @Test
    void testGetOrderById_OrderFound() {
        JsonObject expectedOrderJson = Json.createObjectBuilder()
                .add("id", "65fea17fa9d694237d920f25")
                .add("orderNumber", "Test OrderNumber")
                .add("userId", "Test UserId")
                .add("productId", "Test ProductId")
                .add("totalCost", 30.5)
                .build();

        Mockito.when(orderRepository.getOrderById(ArgumentMatchers.anyString())).thenReturn(Uni.createFrom().item(Response.ok(expectedOrderJson).build()));


        given()
                .when()
                .get("/order/65fea17fa9d694237d920f25")
                .then()
                .statusCode(200);


        Mockito.verify(orderRepository).getOrderById("65fea17fa9d694237d920f25");
    }

    @Test
    void testGetAllOrdersEndpoint() {
        given()
                .when().get("/order")
                .then()
                .statusCode(200);
    }



    @Test
    void testUpdateOrder_Successful() {

        Mockito.when(orderRepository.updateOrder(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble())).thenReturn(Uni.createFrom().nullItem());

        given()
                .contentType(ContentType.JSON)
                .body("{\"orderNumber\": \"Updated OrderNumber\", \"userId\": \"Updated UserId\", \"productId\": \"Updated ProductId\", \"totalCost\": 40.0}")
                .when()
                .put("/order/65fea17fa9d694237d920f25")
                .then()
                .statusCode(200);


        Mockito.verify(orderRepository).updateOrder("65fea17fa9d694237d920f25", "Updated OrderNumber", "Updated UserId", "Updated ProductId", 40.0);
    }

    @Test
    void testDeleteOrder_Successful() {
        Mockito.when(orderRepository.deleteOrder(Mockito.anyString())).thenReturn(Uni.createFrom().nullItem());


        given()
                .when()
                .delete("/order/65fea17fa9d694237d920f25")
                .then()
                .statusCode(200);

        Mockito.verify(orderRepository).deleteOrder("65fea17fa9d694237d920f25");
    }
}