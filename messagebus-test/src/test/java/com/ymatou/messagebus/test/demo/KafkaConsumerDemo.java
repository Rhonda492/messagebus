/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.infrastructure.kafka.KafkaMessageKey;

public class KafkaConsumerDemo {

    private static Logger logger = LoggerFactory.getLogger(KafkaConsumerDemo.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "mqmaster.ymatou.com:9092,mqmaster.ymatou.com:9093,mqmaster.ymatou.com:9094");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Consumer<KafkaMessageKey, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("test"));
        try {
            while (true) {
                ConsumerRecords<KafkaMessageKey, String> records = consumer.poll(500);
                for (TopicPartition partition : records.partitions()) {

                    List<ConsumerRecord<KafkaMessageKey, String>> partitionRecords = records.records(partition);
                    try {
                        for (ConsumerRecord<KafkaMessageKey, String> record : partitionRecords) {
                            logger.info("Recv kafka message:{}", record);
                            // kafkaRecordListener.onRecordReceived(record);
                        }
                    } catch (Exception e) {
                        // 一个Partition消费异常，继续去消费别的Partition
                        logger.error("Failed to consume kafka message", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("failed when consume message", e);
        } finally {
            consumer.close();
        }
    }

}
