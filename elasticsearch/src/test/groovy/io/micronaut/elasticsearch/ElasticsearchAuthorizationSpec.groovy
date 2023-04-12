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
package io.micronaut.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.InfoResponse
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.elasticsearch.ElasticsearchContainer
import spock.lang.Requires
import spock.lang.Specification

@Requires({ sys['elasticsearch.version'] })
class ElasticsearchAuthorizationSpec extends Specification {

    static final String ELASTICSEARCH_VERSION = System.getProperty("elasticsearch.version")
    static final String ELASTICSEARCH_USERNAME = "elastic"
    static final String ELASTICSEARCH_PASSWORD = "changeme"
    static final ElasticsearchContainer container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:$ELASTICSEARCH_VERSION")
            .withExposedPorts(9200)
            .withEnv("xpack.security.enabled", "false")
            .withPassword(ELASTICSEARCH_PASSWORD)
            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*\"message\":\"started\".*"))

    void setupSpec() {
        container.start()
    }

    void cleanupSpec() {
        container.stop()
    }

    void "Test Elasticsearch authorization"() {

        given:

        String token = new String(Base64.getEncoder().encode((ELASTICSEARCH_USERNAME + ':' + ELASTICSEARCH_PASSWORD).getBytes()))

        ApplicationContext applicationContext = ApplicationContext.run(
                'elasticsearch.httpHosts': "http://${container.httpHostAddress}",
                'elasticsearch.default-headers': "Authorization:Basic $token"
        )

        expect:
        applicationContext.containsBean(ElasticsearchClient)
        applicationContext.getBean(ElasticsearchClient).ping()
        InfoResponse response = applicationContext.getBean(ElasticsearchClient).info()
        System.out.println(String.format("cluster: %s, node: %s, version: %s %s", response.clusterName(), response.name(), response.version().number(), response.version().buildDate()))

        cleanup:
        applicationContext.close()

    }

}
