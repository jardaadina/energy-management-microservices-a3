package com.energy.monitoring.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.ingest}")
    private String myIngestQueueName;

    @Value("${rabbitmq.routing-key.ingest}")
    private String ingestRoutingKey;

    @Value("${rabbitmq.queue.sync}")
    private String syncQueue;

    @Value("${rabbitmq.exchange.device-data}")
    private String deviceDataExchange;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;

    @Bean
    public Queue deviceDataQueue() {
        return new Queue(myIngestQueueName, true);
    }

    @Bean
    public TopicExchange deviceDataExchange() {
        return new TopicExchange(deviceDataExchange);
    }

    @Bean
    public Binding deviceDataBinding() {
        return BindingBuilder
                .bind(deviceDataQueue())
                .to(deviceDataExchange())
                .with(ingestRoutingKey);
    }

    @Bean
    public Queue monitoringSyncQueue() {
        return new Queue(syncQueue + ".monitoring", true);
    }

    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(syncExchange);
    }

    @Bean
    public Binding monitoringSyncBinding() {
        return BindingBuilder
                .bind(monitoringSyncQueue())
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