package com.energy.loadbalancer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct; // Asigură-te că ai dependența, sau javax.annotation
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ConsistentHashingService {

    private final SortedMap<Integer, String> circle = new TreeMap<>();

    @Value("${load-balancer.replicas:3}")
    private int numberOfReplicas;

    @Value("${rabbitmq.queue.monitoring-ingest-prefix}")
    private String queuePrefix; // ex: monitoring-ingest-queue

    @Value("${load-balancer.hash.virtual-nodes:150}")
    private int virtualNodesPerReplica;

    @PostConstruct
    public void init() {
        for (int i = 1; i <= numberOfReplicas; i++) {
            String queueName = queuePrefix + "-" + i;
            addReplica(queueName);
            System.out.println("Added replica to hash circle: " + queueName);
        }
    }

    private void addReplica(String replica) {
        for (int i = 0; i < virtualNodesPerReplica; i++) {
            circle.put(hash(replica + i), replica);
        }
    }

    public String getReplicaQueue(String deviceId) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = hash(deviceId);
        if (!circle.containsKey(hash)) {
            SortedMap<Integer, String> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }

    private int hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());
            return ((digest[0] & 0xFF) << 24) |
                    ((digest[1] & 0xFF) << 16) |
                    ((digest[2] & 0xFF) << 8) |
                    ((digest[3] & 0xFF));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}