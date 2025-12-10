package com.energy.websocket_service.config;

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

    @Value("${rabbitmq.exchange.alerts}")
    private String alertExchange;

    @Value("${rabbitmq.queue.overconsumption}")
    private String overconsumptionQueue;

    @Value("${rabbitmq.routing-key.alert}")
    private String alertRoutingKey;

    @Value("${rabbitmq.exchange.chat}")
    private String chatExchange;

    @Value("${rabbitmq.queue.chat-messages}")
    private String chatMessagesQueue;

    @Value("${rabbitmq.routing-key.chat}")
    private String chatRoutingKey;

    @Value("${rabbitmq.routing-key.chat-response}")
    private String chatResponseRoutingKey;

    @Bean
    public Queue adminQueue() {
        return new Queue("chat-admin-notifications", true);
    }

    @Bean
    public Binding adminBinding(Queue adminQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(adminQueue)
                .to(chatExchange)
                .with("chat.admin.notification");
    }

    @Bean
    public TopicExchange alertExchange() {
        return new TopicExchange(alertExchange);
    }

    @Bean
    public Queue overconsumptionQueue() {
        return new Queue(overconsumptionQueue, true);
    }

    @Bean
    public Binding alertBinding(Queue overconsumptionQueue, TopicExchange alertExchange) {
        return BindingBuilder
                .bind(overconsumptionQueue)
                .to(alertExchange)
                .with(alertRoutingKey);
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(chatExchange);
    }

    @Bean
    public Queue chatMessagesQueue() {
        return new Queue(chatMessagesQueue, true);
    }

    @Bean
    public Binding chatBinding(Queue chatMessagesQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(chatMessagesQueue)
                .to(chatExchange)
                .with(chatResponseRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}