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
package io.micronaut.elasticsearch.graalvm;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.micronaut.core.annotation.Internal;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.auth.AuthScheme;
import org.apache.http.client.AuthCache;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.util.Args;
import org.elasticsearch.client.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link org.apache.http.impl.client.BasicAuthCache} used in the {@link org.elasticsearch.client.RestClient} is using
 * serialization which is not supported by GraalVM.
 *
 * We substitute it with an implementation which does not use serialization.
 *
 * Forked from Quarkus: https://github.com/quarkusio/quarkus/blob/c9cba824e8812fa3f15474b8382ac5d90f7238aa/extensions/elasticsearch-rest-client/runtime/src/main/java/io/quarkus/elasticsearch/restclient/runtime/graal/Substitute_RestClient.java
 *
 * @author Iván López
 * @since 2.0.0
 */
@Internal
@TargetClass(className = "org.elasticsearch.client.RestClient")
final class RestClientSubstitutions {

    @Alias
    private ConcurrentMap<HttpHost, DeadHostState> blacklist;

    @Alias
    private volatile NodeTuple<List<Node>> nodeTuple;

    @Substitute
    public synchronized void setNodes(Collection<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("nodes must not be null or empty");
        }
        AuthCache authCache = new NoSerializationBasicAuthCache();

        Map<HttpHost, Node> nodesByHost = new LinkedHashMap<>();
        for (Node node : nodes) {
            Objects.requireNonNull(node, "node cannot be null");
            // TODO should we throw an IAE if we have two nodes with the same host?
            nodesByHost.put(node.getHost(), node);
            authCache.put(node.getHost(), new BasicScheme());
        }
        this.nodeTuple = new NodeTuple<>(Collections.unmodifiableList(new ArrayList<>(nodesByHost.values())),
                authCache);
        this.blacklist.clear();
    }

    @TargetClass(className = "org.elasticsearch.client.DeadHostState")
    static final class DeadHostState {
    }

    @TargetClass(className = "org.elasticsearch.client.RestClient", innerClass = "NodeTuple")
    static final class NodeTuple<T> {

        @Alias
        NodeTuple(final T nodes, final AuthCache authCache) {
        }
    }

    @Contract(threading = ThreadingBehavior.SAFE)
    private static final class NoSerializationBasicAuthCache implements AuthCache {

        private final Map<HttpHost, AuthScheme> map;
        private final SchemePortResolver schemePortResolver;

        public NoSerializationBasicAuthCache(final SchemePortResolver schemePortResolver) {
            this.map = new ConcurrentHashMap<>();
            this.schemePortResolver = schemePortResolver != null ? schemePortResolver
                    : DefaultSchemePortResolver.INSTANCE;
        }

        public NoSerializationBasicAuthCache() {
            this(null);
        }

        protected HttpHost getKey(final HttpHost host) {
            if (host.getPort() <= 0) {
                final int port;
                try {
                    port = schemePortResolver.resolve(host);
                } catch (final UnsupportedSchemeException ignore) {
                    return host;
                }
                return new HttpHost(host.getHostName(), port, host.getSchemeName());
            } else {
                return host;
            }
        }

        @Override
        public void put(final HttpHost host, final AuthScheme authScheme) {
            Args.notNull(host, "HTTP host");
            if (authScheme == null) {
                return;
            }
            this.map.put(getKey(host), authScheme);
        }

        @Override
        public AuthScheme get(final HttpHost host) {
            Args.notNull(host, "HTTP host");
            return this.map.get(getKey(host));
        }

        @Override
        public void remove(final HttpHost host) {
            Args.notNull(host, "HTTP host");
            this.map.remove(getKey(host));
        }

        @Override
        public void clear() {
            this.map.clear();
        }

        @Override
        public String toString() {
            return this.map.toString();
        }
    }
}
