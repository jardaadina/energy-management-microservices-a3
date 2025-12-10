package com.energy.loadbalancer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.device-data}")
    private String deviceDataQueueName;

    @Value("${rabbitmq.exchange.device-data}")
    private String deviceDataExchangeName;

    @Value("${rabbitmq.routing-key.device-data}")
    private String deviceDataRoutingKey;

    @Bean
    public Queue inputQueue() {
        return new Queue(deviceDataQueueName, true);
    }

    @Bean
    public TopicExchange deviceDataExchange() {
        return new TopicExchange(deviceDataExchangeName);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(inputQueue())
                .to(deviceDataExchange())
                .with(deviceDataRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}