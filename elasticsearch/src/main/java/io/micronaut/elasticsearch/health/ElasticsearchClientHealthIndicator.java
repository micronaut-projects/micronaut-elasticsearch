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
import java.io.IOException;

import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.UP;

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
@Requires(property = HealthEndpoint.PREFIX + ".elasticsearch.rest.high.level.enabled", notEquals = "true") // we don't want to clash with the deprecated check
@Singleton
public class ElasticsearchClientHealthIndicator implements HealthIndicator {

    private static final String NAME = "elasticsearchclient";

    private final ElasticsearchAsyncClient client;

    /**
     * Constructor.
     *
     * @param esClient The Elasticsearch high level REST client.
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
        return (subscriber -> {
            final HealthResult.Builder resultBuilder = HealthResult.builder(NAME);
            try {
                client.cluster().health().handle((health, exception) -> {
                    if(exception != null) {
                        subscriber.onNext(resultBuilder.status(DOWN).exception(exception).build());
                        subscriber.onComplete();
                    } else {
                        HealthStatus status = health.status() == co.elastic.clients.elasticsearch._types.HealthStatus.Red ? DOWN : UP;
                        subscriber.onNext(resultBuilder.status(status).details(health).build());
                        subscriber.onComplete();
                    }
                    return health;
                });
            } catch (IOException e) {
                subscriber.onNext(resultBuilder.status(DOWN).exception(e).build());
                subscriber.onComplete();
            }
        });
    }
}
