/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.messagebus.test.kafka;

import com.ymatou.messagebus.infrastructure.kafka.KafkaMessageKey;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KafkaProducerDemo {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.16.103.18:9092");
        props.put("acks", "1");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");




        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            KafkaMessageKey kafkaMessageKey =
                    new KafkaMessageKey("testjava_kafka", "hello", UUID.randomUUID().toString(), String.valueOf(i));
            ProducerRecord<String, String> record =
                    new ProducerRecord<String, String>("messagebus.testjava_kafka_hello", kafkaMessageKey.toString(), String.valueOf(i));
            producer.send(record);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();

        System.out.println("kafka send ok.");
    }


}
