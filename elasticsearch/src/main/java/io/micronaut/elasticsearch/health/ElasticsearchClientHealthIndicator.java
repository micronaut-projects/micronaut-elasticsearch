/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.elasticsearch.health;

import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import org.reactivestreams.Publisher;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import jakarta.inject.Singleton;

import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.UP;
import java.util.Locale;

/**
 * A {@link HealthIndicator} for Elasticsearch that uses an automatically-configured high-level REST client, injected as a dependency, to communicate
 * with Elasticsearch.
 *
 * @author Puneet Behl
 * @author Robyn Dalgleish
 * @since 1.0.0
 */
@Requires(beans = HealthEndpoint.class)
@Requires(property = HealthEndpoint.PREFIX + ".elasticsearch.enabled", notEquals = "false")
@Singleton
public class ElasticsearchClientHealthIndicator implements HealthIndicator {

    private static final String NAME = "elasticsearchclient";

    private final ElasticsearchAsyncClient client;

    /**
     * Constructor.
     *
     * @param client The Elasticsearch high level REST client.
     */
    public ElasticsearchClientHealthIndicator(ElasticsearchAsyncClient client) {
        this.client = client;
    }

    /**
     * Tries to call the cluster info API on Elasticsearch to obtain information about the cluster. If the call succeeds, the Elasticsearch cluster
     * health status (GREEN / YELLOW / RED) will be included in the health indicator details.
     *
     * @return A positive health result UP if the cluster can be communicated with and is in either GREEN or YELLOW status. A negative health result
     * DOWN if the cluster cannot be communicated with or is in RED status.
     */
    @Override
    public Publisher<HealthResult> getResult() {
        return (subscriber -> client.cluster().health()
            .handle((health, exception) -> {
                final HealthResult.Builder resultBuilder = HealthResult.builder(NAME);
                if (exception != null) {
                    subscriber.onNext(resultBuilder.status(DOWN).exception(exception).build());
                    subscriber.onComplete();
                } else {
                    HealthStatus status = health.status() == co.elastic.clients.elasticsearch._types.HealthStatus.Red ? DOWN : UP;
                    subscriber.onNext(resultBuilder.status(status).details(healthResultDetails(health)).build());
                    subscriber.onComplete();
                }
                return health;
            }));
    }

    private String healthResultDetails(HealthResponse response) {
        return "{"
            + "\"cluster_name\":\"" + response.clusterName() + "\","
            + "\"status\":\"" + response.status().name().toLowerCase(Locale.ENGLISH) + "\","
            + "\"timed_out\":" + response.timedOut() + ","
            + "\"number_of_nodes\":" + response.numberOfNodes() + ","
            + "\"number_of_data_nodes\":" + response.numberOfDataNodes() + ","
            + "\"number_of_pending_tasks\":" + response.numberOfPendingTasks() + ","
            + "\"number_of_in_flight_fetch\":" + response.numberOfInFlightFetch() + ","
            + "\"task_max_waiting_in_queue\":\"" + response.taskMaxWaitingInQueueMillis() + "\","
            + "\"task_max_waiting_in_queue_millis\":" + response.taskMaxWaitingInQueueMillis() + ","
            + "\"active_shards_percent_as_number\":\"" + response.activeShardsPercentAsNumber() + "\","
            + "\"active_primary_shards\":" + response.activePrimaryShards() + ","
            + "\"active_shards\":" + response.activeShards() + ","
            + "\"relocating_shards\":" + response.relocatingShards() + ","
            + "\"initializing_shards\":" + response.initializingShards() + ","
            + "\"unassigned_shards\":" + response.unassignedShards() + ","
            + "\"delayed_unassigned_shards\":" + response.delayedUnassignedShards()
            + "}";
    }
}


