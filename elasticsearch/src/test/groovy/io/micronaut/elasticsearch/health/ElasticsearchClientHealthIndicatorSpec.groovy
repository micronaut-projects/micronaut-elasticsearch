/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.elasticsearch.health

import groovy.json.JsonSlurper
import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.NoSuchBeanException
import io.micronaut.elasticsearch.DefaultElasticsearchConfigurationProperties
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.elasticsearch.ElasticsearchContainer
import reactor.core.publisher.Flux
import spock.lang.Requires
import spock.lang.Specification
/**
 * @author Puneet Behl
 * @since 1.0.0
 */
@Requires({ sys['elasticsearch.version'] })
class ElasticsearchClientHealthIndicatorSpec extends Specification {

    final static String ELASTICSEARCH_VERSION = System.getProperty("elasticsearch.version")

    void "test elasticsearch health indicator"() {
        given:
        ElasticsearchContainer container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:$ELASTICSEARCH_VERSION")
                .withExposedPorts(9200)
                .withEnv("xpack.security.enabled", "false")
                .waitingFor(new LogMessageWaitStrategy().withRegEx(".*\"message\":\"started.*"))
        container.start()

        ApplicationContext applicationContext = ApplicationContext.run('elasticsearch.httpHosts': "http://$container.httpHostAddress")

        expect:
        applicationContext.containsBean(DefaultElasticsearchConfigurationProperties)

        when:
        ElasticsearchClientHealthIndicator indicator = applicationContext.getBean(ElasticsearchClientHealthIndicator)
        HealthResult result = Flux.from(indicator.getResult()).blockFirst()

        then:
        result.status == HealthStatus.UP
        new JsonSlurper().parseText((String) result.details).status == co.elastic.clients.elasticsearch._types.HealthStatus.Green.name().toLowerCase(Locale.ENGLISH)

        when:
        container.stop()
        result = Flux.from(indicator.getResult()).blockFirst()

        then:
        result.status == HealthStatus.DOWN

        cleanup:
        applicationContext?.stop()
        container.stop()
    }

    void "test that ElasticsearchClientHealthIndicator is not created when the endpoints.health.elasticsearch.rest.high.level.enabled is set to false "() {
        ApplicationContext applicationContext = ApplicationContext.run(
                'elasticsearch.httpHosts': "http://localhost:9200",
                'endpoints.health.elasticsearch.enabled': "false"
        )

        when:
        applicationContext.getBean(ElasticsearchClientHealthIndicator)

        then:
        thrown(NoSuchBeanException)

        cleanup:
        applicationContext?.stop()
    }

}
