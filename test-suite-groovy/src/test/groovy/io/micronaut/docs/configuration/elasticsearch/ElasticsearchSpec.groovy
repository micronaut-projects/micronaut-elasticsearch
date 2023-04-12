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

package io.micronaut.docs.configuration.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.InfoResponse
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.elasticsearch.DefaultElasticsearchConfigurationProperties
//tag::httpClientFactoryImports[]

import jakarta.inject.Singleton
import org.apache.http.auth.AuthScope
//end::httpClientFactoryImports[]

import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.elasticsearch.ElasticsearchContainer
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
//tag::singletonImports[]
//end::singletonImports[]
/**
 * @author Puneet Behl
 * @since 1.0.0
 */
@Requires({ sys['elasticsearch.version'] })
class ElasticsearchSpec extends Specification {

    // tag::es-testcontainer[]
    final static String ELASTICSEARCH_VERSION = System.getProperty("elasticsearch.version")

    @Shared
    static final ElasticsearchContainer container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:$ELASTICSEARCH_VERSION")
            .withExposedPorts(9200)
            .withEnv("xpack.security.enabled", "false")
            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*\"message\":\"started\".*"))
    // end::es-testcontainer[]

    void setupSpec() {
        container.start()
    }

    void cleanupSpec() {
        container.stop()
    }

    void "Test simple info for Elasticsearch stats using the ElasticsearchClient"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run("elasticsearch.httpHosts": "http://$container.httpHostAddress", "test")
        String stats

        when:
        ElasticsearchClient client = applicationContext.getBean(ElasticsearchClient)
        //tag::query[]
        InfoResponse response =
                client.info() // <1>
        //end::query[]

        then:
        "docker-cluster" == response.clusterName()
        ELASTICSEARCH_VERSION == response.version().number()

        cleanup:
        applicationContext.close()
    }

    void "Test simple info for Elasticsearch stats using the ElasticsearchAsyncClient"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run("elasticsearch.httpHosts": "http://$container.httpHostAddress", "test")
        String stats

        when:
        ElasticsearchAsyncClient client = applicationContext.getBean(ElasticsearchAsyncClient)
        InfoResponse response =
                client.info().get() // <1>

        then:
        "docker-cluster" == response.clusterName()
        ELASTICSEARCH_VERSION == response.version().number()

        cleanup:
        applicationContext.close()
    }

    void "Test overiding HttpAsyncClientBuilder bean"() {

        when:
        ApplicationContext applicationContext = ApplicationContext.run("elasticsearch.httpHosts": "http://127.0.0.1:9200,http://127.0.1.1:9200")

        then:
        applicationContext.containsBean(HttpAsyncClientBuilder)
        applicationContext.getBean(DefaultElasticsearchConfigurationProperties).httpAsyncClientBuilder

        cleanup:
        applicationContext.close()

    }

    //tag::httpClientFactory[]
    @Factory
    static class HttpAsyncClientBuilderFactory {

        @Replaces(HttpAsyncClientBuilder.class)
        @Singleton
        HttpAsyncClientBuilder builder() {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider()
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials("user", "password"))

            return HttpAsyncClientBuilder.create()
                    .setDefaultCredentialsProvider(credentialsProvider)
        }
    }
    //end::httpClientFactory[]
}
