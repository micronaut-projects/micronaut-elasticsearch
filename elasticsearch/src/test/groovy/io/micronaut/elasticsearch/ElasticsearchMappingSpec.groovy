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
import co.elastic.clients.elasticsearch._types.Result
import co.elastic.clients.elasticsearch.core.*
import co.elastic.clients.elasticsearch.indices.ExistsRequest
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.elasticsearch.ElasticsearchContainer
import spock.lang.Requires
import spock.lang.Specification

/**
 * @author lishuai
 * @author Puneet Behl
 * @since 1.0.1
 */
@Requires({ sys['elasticsearch.version'] })
class ElasticsearchMappingSpec extends Specification {

    final static String ELASTICSEARCH_VERSION = System.getProperty("elasticsearch.version")
    static final ElasticsearchContainer container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:$ELASTICSEARCH_VERSION")
            .withExposedPorts(9200)
            .withEnv("xpack.security.enabled", "false")
            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*\"message\":\"started.*"))

    void setupSpec() {
        container.start()
    }

    void cleanupSpec() {
        container.stop()
    }


    void "Test Elasticsearch connection"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run('elasticsearch.httpHosts': 'http://' + container.httpHostAddress)

        expect:
        applicationContext.containsBean(ElasticsearchClient)
        applicationContext.getBean(ElasticsearchClient).ping()
        InfoResponse response = applicationContext.getBean(ElasticsearchClient).info()
        System.out.println(String.format("cluser: %s, node: %s, version: %s %s", response.clusterName(), response.name(), response.version().number(), response.version().buildDate()))

        cleanup:
        applicationContext.close()
    }

    void "Test Elasticsearch(8.x) Mapping API"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run('elasticsearch.httpHosts': 'http://' + container.getHttpHostAddress())
        ElasticsearchClient client = applicationContext.getBean(ElasticsearchClient)

        expect: "Make sure the version of ES is up to date because these tests may cause unexpected results"
        ELASTICSEARCH_VERSION == client.info().version().number()

        when:
        ExistsRequest existsRequest = new ExistsRequest.Builder().index("posts").build()

        then: "index does not exists"
        !client.indices().exists(existsRequest).value()

        when: "create index request"
        IndexRequest.Builder<?> requestBuilder = new IndexRequest.Builder<>()
                .index("posts")
                .id("1")

        Map<String, Object> document = new HashMap<>()
        document.put("user", "kimchy")
        document.put("postDate", "2013-01-30")
        document.put("message", "trying out Elasticsearch")
        requestBuilder.document(document)

        IndexResponse response = client.index(requestBuilder.build())

        then: "verify version and result"
        response.index() == "posts"
        response.version() == 1
        response.result() == Result.Created

        when: "update index request"
        requestBuilder = new IndexRequest.Builder<>()
                .index("posts")
                .id("1")

        document = new HashMap<>()
        document.put("user", "kimchy1")
        document.put("postDate", "2018-10-30")
        document.put("message", "Trying out Elasticsearch6")
        requestBuilder.document(document)

        response = client.index(requestBuilder.build())

        then: "verify version and result"
        response.index() == "posts"
        response.version() == 2
        response.result() == Result.Updated

        when: "get request"
        GetRequest getRequest = new GetRequest.Builder()
                .index("posts")
                .id("1")
                .sourceIncludes("message", "*Date")
                .build()

        GetResponse<Map> getResponse = client.get(getRequest, Map.class)

        then: "verify source"
        getResponse.index() == "posts"
        getResponse.version() == 2
        getResponse.source() == [postDate: "2018-10-30", message: "Trying out Elasticsearch6"]

        when: "exits request"
        co.elastic.clients.elasticsearch.core.ExistsRequest existsRequest2 = new co.elastic.clients.elasticsearch.core.ExistsRequest.Builder()
                .index("posts")
                .id("1")
                .build()

        then:
        client.exists(existsRequest2)

        when: "delete request"
        DeleteRequest deleteRequest = new DeleteRequest.Builder()
                .index("posts")
                .id("1")
                .build()
        DeleteResponse deleteResponse = client.delete(deleteRequest)

        then:
        deleteResponse.index() == "posts"

        cleanup:
        applicationContext.close()
    }
}
