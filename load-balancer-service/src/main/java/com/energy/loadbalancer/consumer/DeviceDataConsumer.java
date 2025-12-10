package com.energy.loadbalancer.consumer;

import com.energy.loadbalancer.dto.DeviceMeasurement;
import com.energy.loadbalancer.service.ConsistentHashingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceDataConsumer {

    private final ConsistentHashingService hashingService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public DeviceDataConsumer(ConsistentHashingService hashingService, RabbitTemplate rabbitTemplate) {
        this.hashingService = hashingService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${rabbitmq.queue.device-data}")
    public void consumeMessage(DeviceMeasurement measurement) {
        String targetQueue = hashingService.getReplicaQueue(measurement.getDeviceId());

        if (targetQueue != null) {
            System.out.println("LB: Redirecting Device " + measurement.getDeviceId() + " -> " + targetQueue);
            rabbitTemplate.convertAndSend(targetQueue, measurement);
        } else {
            System.err.println("LB Error: No monitoring replica available!");
        }
    }
}