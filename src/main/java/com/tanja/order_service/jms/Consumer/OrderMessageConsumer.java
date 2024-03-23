package com.tanja.order_service.jms.Consumer;

import com.tanja.order_service.jms.JMSContextManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.jms.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderMessageConsumer {

    @Inject
    private JMSContextManager contextManager;

    @Inject
    @Resource(mappedName = "java:/jms/queue/OrderServiceQueue")
    private Queue logQueue;

    private final Logger logger = LoggerFactory.getLogger(OrderMessageConsumer.class);

    private JMSConsumer consumer;

    @PostConstruct
    public void init() {
        JMSContext context = contextManager.getContext();
        consumer = context.createConsumer(logQueue);
        consumer.setMessageListener(this::processMessage);
    }

    private void processMessage(Message message) {
        try {
            String logMessage = message.getBody(String.class);
            logger.info("Received log message: {}", logMessage);
            // Additional processing logic here if needed
        } catch (JMSException e) {
            logger.error("Failed to process log message", e);
        }
    }

}
