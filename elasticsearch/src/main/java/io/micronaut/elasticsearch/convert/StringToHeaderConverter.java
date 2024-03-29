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
package io.micronaut.elasticsearch.convert;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClientBuilder;

import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * Converts String to {@link Header}.
 *
 * @author Puneet Behl
 * @since 1.0.0
 */
@Singleton
@Requires(classes = RestClientBuilder.class)
public class StringToHeaderConverter implements TypeConverter<CharSequence, Header> {

    @Override
    public Optional<Header> convert(CharSequence object, Class<Header> targetType, ConversionContext context) {
        String header = object.toString();
        if (header.contains(":")) {
            String[] nameAndValue = header.split(":");
            return Optional.of(new BasicHeader(nameAndValue[0], nameAndValue[1]));
        } else {
            return Optional.empty();
        }
    }
}
