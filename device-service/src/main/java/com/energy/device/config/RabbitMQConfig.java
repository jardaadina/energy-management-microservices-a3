package com.energy.device.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.sync}")
    private String syncQueue;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;

    // Queue specifică pentru device-service
    @Bean
    public Queue deviceSyncQueue() {
        return new Queue(syncQueue + ".device", true);
    }

    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(syncExchange);
    }

    // Binding pentru device-service
    @Bean
    public Binding deviceSyncBinding() {
        return BindingBuilder
                .bind(deviceSyncQueue())
                .to(syncExchange())
                .with("sync.#");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}