package com.tanja.order_service.jms;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

@ApplicationScoped
public class JMSContextManager {

        @Inject
        private ConnectionFactory connectionFactory;

        private JMSContext context;

        @PostConstruct
        public void initialize() {
            context = connectionFactory.createContext();
        }

        @PreDestroy
        public void close() {
            if (context != null) {
                context.close();
            }
        }

        public JMSContext getContext() {
            return context;
        }
    }
