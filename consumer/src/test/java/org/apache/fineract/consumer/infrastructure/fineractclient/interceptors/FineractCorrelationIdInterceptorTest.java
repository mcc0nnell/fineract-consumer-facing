/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.consumer.infrastructure.fineractclient.interceptors;

import static org.assertj.core.api.Assertions.assertThat;

import feign.Request;
import feign.RequestTemplate;
import org.apache.fineract.consumer.infrastructure.correlation.service.CorrelationIdContext;
import org.apache.fineract.consumer.infrastructure.fineractclient.data.FineractHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FineractCorrelationIdInterceptorTest {

    private static final String CORRELATION_ID = "corr-123";
    private final FineractCorrelationIdInterceptor interceptor = new FineractCorrelationIdInterceptor();

    @AfterEach
    void clearContext() {
        CorrelationIdContext.clear();
    }

    @Test
    void setsCorrelationHeaderOnGet() {
        CorrelationIdContext.set(CORRELATION_ID);
        RequestTemplate template = template(Request.HttpMethod.GET);

        interceptor.apply(template);

        assertThat(template.headers().get(FineractHeaders.CORRELATION_ID)).containsExactly(CORRELATION_ID);
    }

    @Test
    void setsCorrelationHeaderOnWrite() {
        CorrelationIdContext.set(CORRELATION_ID);
        RequestTemplate template = template(Request.HttpMethod.POST);

        interceptor.apply(template);

        assertThat(template.headers().get(FineractHeaders.CORRELATION_ID)).containsExactly(CORRELATION_ID);
    }

    @Test
    void doesNotSetCorrelationHeaderOutsideRequestContext() {
        RequestTemplate template = template(Request.HttpMethod.GET);

        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey(FineractHeaders.CORRELATION_ID);
    }

    private static RequestTemplate template(Request.HttpMethod method) {
        RequestTemplate template = new RequestTemplate();
        template.method(method);
        return template;
    }
}
