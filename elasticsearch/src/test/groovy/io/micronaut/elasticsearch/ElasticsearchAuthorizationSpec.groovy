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

import io.micronaut.context.ApplicationContext
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.core.MainResponse
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Requires
import spock.lang.Specification

@Requires({ sys['elasticsearch.version'] })
class ElasticsearchAuthorizationSpec extends Specification {

    static final String ELASTICSEARCH_VERSION = System.getProperty("elasticsearch.version")
    static final String ELASTICSEARCH_USERNAME = "elastic"
    static final String ELASTICSEARCH_PASSWORD = "changeme"

    void "Test Elasticsearch authorization"() {

        given:
        ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse("elasticsearch:$ELASTICSEARCH_VERSION").asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
        container.withPassword(ELASTICSEARCH_PASSWORD)
        container.start()

        String token = new String(Base64.getEncoder().encode((ELASTICSEARCH_USERNAME + ':' + ELASTICSEARCH_PASSWORD).getBytes()))

        ApplicationContext applicationContext = ApplicationContext.run(
                'elasticsearch.httpHosts': 'http://' + container.getHttpHostAddress(),
                'elasticsearch.default-headers': "Authorization:Basic ${token}"
        )

        expect:
        applicationContext.containsBean(RestHighLevelClient)
        applicationContext.getBean(RestHighLevelClient).ping(RequestOptions.DEFAULT)
        MainResponse response = applicationContext.getBean(RestHighLevelClient).info(RequestOptions.DEFAULT)
        System.out.println(String.format("cluster: %s, node: %s, version: %s %s", response.getClusterName(), response.getNodeName(), response.getVersion().getNumber(), response.getVersion().getBuildDate()))

        cleanup:
        applicationContext.close()
        container.stop()
    }

}
