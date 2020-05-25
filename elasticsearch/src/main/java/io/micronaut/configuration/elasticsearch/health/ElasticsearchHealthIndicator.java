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
package io.micronaut.configuration.elasticsearch.health;

import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.io.IOException;

import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.UP;
import static java.util.Collections.emptyMap;
import static org.elasticsearch.cluster.health.ClusterHealthStatus.GREEN;
import static org.elasticsearch.cluster.health.ClusterHealthStatus.YELLOW;

/**
 * A {@link HealthIndicator} for Elasticsearch that uses an automatically-configured high-level REST client, injected as a dependency, to communicate
 * with Elasticsearch.
 *
 * @author Puneet Behl
 * @author Robyn Dalgleish
 * @since 1.0.0
 */
@Requires(beans = HealthEndpoint.class)
@Requires(property = HealthEndpoint.PREFIX + ".elasticsearch.rest.high.level.enabled", notEquals = "false")
@Singleton
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private static final String NAME = "elasticsearch";

    private final RestHighLevelClient esClient;

    /**
     * Constructor.
     *
     * @param esClient The Elasticsearch high level REST client.
     */
    public ElasticsearchHealthIndicator(RestHighLevelClient esClient) {
        this.esClient = esClient;
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

        return (subscriber -> esClient.cluster().healthAsync(new ClusterHealthRequest(), RequestOptions.DEFAULT, new ActionListener<ClusterHealthResponse>() {

            private final HealthResult.Builder resultBuilder = HealthResult.builder(NAME);

            @Override
            public void onResponse(ClusterHealthResponse response) {

                HealthResult result;

                try {
                    result = resultBuilder
                        .status(healthResultStatus(response))
                        .details(healthResultDetails(response))
                        .build();
                } catch (IOException e) {
                    result = resultBuilder.status(DOWN).exception(e).build();
                }

                subscriber.onNext(result);
                subscriber.onComplete();
            }

            @Override
            public void onFailure(Exception e) {
                subscriber.onNext(resultBuilder.status(DOWN).exception(e).build());
                subscriber.onComplete();
            }
        }));
    }

    private String healthResultDetails(ClusterHealthResponse response) throws IOException {
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
        response.toXContent(xContentBuilder, new ToXContent.MapParams(emptyMap()));
        return Strings.toString(xContentBuilder);
    }

    private HealthStatus healthResultStatus(ClusterHealthResponse response) {
        return response.getStatus() == GREEN || response.getStatus() == YELLOW ? UP : DOWN;
    }
}
