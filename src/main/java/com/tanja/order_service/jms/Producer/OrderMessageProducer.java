package com.tanja.order_service.jms.Producer;

import com.tanja.order_service.jms.JMSContextManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

@ApplicationScoped
public class OrderMessageProducer {

    @Inject
    private JMSContextManager contextManager;

    private JMSContext context;

    @PostConstruct
    public void initialize() {
        context = contextManager.getContext();
    }

    @PreDestroy
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    public void sendMessage(String message, Queue destination) {
        context.createProducer().send(destination, message);
    }
}
