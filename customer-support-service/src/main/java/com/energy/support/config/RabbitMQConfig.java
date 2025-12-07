package com.energy.support.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.chat}")
    private String chatExchange;

    @Value("${rabbitmq.queue.chat-requests}")
    private String chatRequestsQueue;

    @Value("${rabbitmq.queue.chat-responses}")
    private String chatResponsesQueue;

    @Value("${rabbitmq.routing-key.chat-request}")
    private String chatRequestRoutingKey;

    @Value("${rabbitmq.routing-key.chat-response}")
    private String chatResponseRoutingKey;

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(chatExchange);
    }

    @Bean
    public Queue chatRequestsQueue() {
        return new Queue(chatRequestsQueue, true);
    }

    @Bean
    public Queue chatResponsesQueue() {
        return new Queue(chatResponsesQueue, true);
    }

    @Bean
    public Binding chatRequestBinding(Queue chatRequestsQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(chatRequestsQueue)
                .to(chatExchange)
                .with(chatRequestRoutingKey);
    }

    @Bean
    public Binding chatResponseBinding(Queue chatResponsesQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(chatResponsesQueue)
                .to(chatExchange)
                .with(chatResponseRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}