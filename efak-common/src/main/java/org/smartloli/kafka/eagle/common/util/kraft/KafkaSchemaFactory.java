/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.common.util.kraft;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Get the Kafka topic metadata information through the broker address.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 07, 2021
 */
public class KafkaSchemaFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSchemaFactory.class);
    private final KafkaStoragePlugin plugin;
    private Set<String> tableNames;

    public KafkaSchemaFactory(final KafkaStoragePlugin plugin) {
        this.plugin = plugin;
    }

    public Set<String> getTableNames(String clusterAlias) {
        if (tableNames == null) {
            KafkaConsumer<?, ?> kafkaConsumer = null;
            try {
                kafkaConsumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(clusterAlias));
                tableNames = kafkaConsumer.listTopics().keySet();
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Failure while loading table names for database '{}': {}", clusterAlias, e);
            } finally {
                plugin.registerToClose(kafkaConsumer);
            }
        }
        return tableNames;
    }

    public void getTopicMetaData(String clusterAlias, List<String> topics) {
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(clusterAlias));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topics);
            System.out.println(describeTopicsResult.all().get());
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Failure while loading topic meta for kafka '{}': {}", clusterAlias, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
    }

    public static void main(String[] args) {
        String[] kafkaClusters = SystemConfigUtils.getPropertyArray("efak.kafka.cluster.alias", ",");
        for (String kafkaCluster : kafkaClusters) {
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            ksf.getTopicMetaData(kafkaCluster, Arrays.asList("test16", "lb_te"));
        }
    }
}
