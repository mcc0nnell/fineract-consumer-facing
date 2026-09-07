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
package org.apache.fineract.consumer.infrastructure.correlation.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.consumer.infrastructure.correlation.service.CorrelationIdContext;
import org.apache.fineract.consumer.infrastructure.web.data.ConsumerHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private static final String CORRELATION_ID = "dig-test-123";
    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearContext() {
        CorrelationIdContext.clear();
    }

    @Test
    void preservesSafeIncomingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> observed = new AtomicReference<>();
        request.addHeader(ConsumerHeaders.CORRELATION_ID, CORRELATION_ID);

        filter.doFilter(request, response, captureContext(observed));

        assertThat(observed.get()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader(ConsumerHeaders.CORRELATION_ID)).isEqualTo(CORRELATION_ID);
        assertThat(CorrelationIdContext.get()).isNull();
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> observed = new AtomicReference<>();

        filter.doFilter(request, response, captureContext(observed));

        assertThat(UUID.fromString(observed.get())).isNotNull();
        assertThat(response.getHeader(ConsumerHeaders.CORRELATION_ID)).isEqualTo(observed.get());
        assertThat(CorrelationIdContext.get()).isNull();
    }

    @Test
    void replacesUnsafeIncomingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> observed = new AtomicReference<>();
        request.addHeader(ConsumerHeaders.CORRELATION_ID, "bad\nheader");

        filter.doFilter(request, response, captureContext(observed));

        assertThat(observed.get()).isNotEqualTo("bad\nheader");
        assertThat(UUID.fromString(observed.get())).isNotNull();
        assertThat(response.getHeader(ConsumerHeaders.CORRELATION_ID)).isEqualTo(observed.get());
    }

    @Test
    void replacesOverlongIncomingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> observed = new AtomicReference<>();
        request.addHeader(ConsumerHeaders.CORRELATION_ID, "a".repeat(CorrelationIdFilter.MAX_LENGTH + 1));

        filter.doFilter(request, response, captureContext(observed));

        assertThat(UUID.fromString(observed.get())).isNotNull();
        assertThat(response.getHeader(ConsumerHeaders.CORRELATION_ID)).isEqualTo(observed.get());
    }

    @Test
    void clearsContextWhenDownstreamThrows() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (req, res) -> {
            assertThat(CorrelationIdContext.get()).isNotNull();
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(CorrelationIdContext.get()).isNull();
        assertThat(response.getHeader(ConsumerHeaders.CORRELATION_ID)).isNotBlank();
    }

    private static FilterChain captureContext(AtomicReference<String> observed) {
        return (request, response) -> observed.set(CorrelationIdContext.get());
    }
}
