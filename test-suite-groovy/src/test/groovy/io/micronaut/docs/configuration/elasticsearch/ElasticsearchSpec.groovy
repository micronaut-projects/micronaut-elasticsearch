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

import io.micronaut.elasticsearch.DefaultElasticsearchConfigurationProperties
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory

//tag::httpClientFactoryImports[]
import io.micronaut.context.annotation.Replaces
import org.apache.http.auth.AuthScope

//end::httpClientFactoryImports[]
import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.InfoResponse
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.Version
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.core.MainResponse
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

import jakarta.inject.Singleton

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
    ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            DockerImageName.parse("elasticsearch:$ELASTICSEARCH_VERSION")
                    .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch")
    )
    // end::es-testcontainer[]


    //tag::es-stats[]
    void "Test simple info for Elasticsearch stats using the High Level REST Client"() {
        given:
        //tag::es-conf[]
        elasticsearch.start()
        ApplicationContext applicationContext = ApplicationContext.run("elasticsearch.httpHosts": "http://${elasticsearch.getHttpHostAddress()}", "test")
        //end::es-conf
        String stats

        when:
        //tag::es-bean[]
        RestHighLevelClient client = applicationContext.getBean(RestHighLevelClient)
        //end::es-bean[]
        MainResponse response =
                client.info(RequestOptions.DEFAULT) // <1>

        then:
        "docker-cluster" == response.getClusterName()
        Version.fromString(ELASTICSEARCH_VERSION).toString() == response.getVersion().getNumber()

        cleanup:
        applicationContext.close()
        elasticsearch.stop()
    }
    //end::es-dbstats[]

    void "Test simple info for Elasticsearch stats using the ElasticsearchClient"() {
        given:
        elasticsearch.start()
        ApplicationContext applicationContext = ApplicationContext.run("elasticsearch.httpHosts": "http://${elasticsearch.getHttpHostAddress()}", "test")
        String stats

        when:
        ElasticsearchClient client = applicationContext.getBean(ElasticsearchClient)
        //tag::query[]
        InfoResponse response =
                client.info() // <1>
        //end::query[]

        then:
        "docker-cluster" == response.clusterName()
        Version.fromString(ELASTICSEARCH_VERSION).toString() == response.version().number()

        cleanup:
        applicationContext.close()
        elasticsearch.stop()
    }

    void "Test simple info for Elasticsearch stats using the ElasticsearchAsyncClient"() {
        given:
        elasticsearch.start()
        ApplicationContext applicationContext = ApplicationContext.run("elasticsearch.httpHosts": "http://${elasticsearch.getHttpHostAddress()}", "test")
        String stats

        when:
        ElasticsearchAsyncClient client = applicationContext.getBean(ElasticsearchAsyncClient)
        InfoResponse response =
                client.info().get() // <1>

        then:
        "docker-cluster" == response.clusterName()
        Version.fromString(ELASTICSEARCH_VERSION).toString() == response.version().number()

        cleanup:
        applicationContext.close()
        elasticsearch.stop()
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
