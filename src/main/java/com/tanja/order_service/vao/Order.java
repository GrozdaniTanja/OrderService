package com.tanja.order_service.vao;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.*;

import java.util.Date;


@EqualsAndHashCode(callSuper = false)
@Data
@MongoEntity(database = "orderDB", collection="order")
public class Order extends ReactivePanacheMongoEntity  {

    public String orderNumber;
    public String userId;
    public String productId;
    public double totalCost;
}
