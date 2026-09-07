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
package org.apache.fineract.consumer.audit.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.apache.fineract.consumer.audit.command.domain.AuditEvent;
import org.apache.fineract.consumer.audit.command.repository.AuditEventCommandRepository;
import org.apache.fineract.consumer.infrastructure.audit.data.AuditEventType;
import org.apache.fineract.consumer.infrastructure.audit.data.NonTransactionalAuditEvent;
import org.apache.fineract.consumer.infrastructure.audit.data.TransactionalAuditEvent;
import org.apache.fineract.consumer.infrastructure.correlation.service.CorrelationIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

class CorrelationIdAuditTest {

    private static final String CORRELATION_ID = "corr-audit-123";

    @AfterEach
    void clearContext() {
        CorrelationIdContext.clear();
    }

    @Test
    void transactionalAuditEventCapturesCorrelationIdBeforeAsyncDelivery() {
        CorrelationIdContext.set(CORRELATION_ID);

        TransactionalAuditEvent event = TransactionalAuditEvent.of(
                AuditEventType.LOGIN_SUCCESS, 7L, false, "device", Map.of("flow", "login"));

        CorrelationIdContext.clear();

        assertThat(event.getCorrelationId()).isEqualTo(CORRELATION_ID);
    }

    @Test
    void nonTransactionalAuditEventCapturesCorrelationIdBeforeAsyncDelivery() {
        CorrelationIdContext.set(CORRELATION_ID);

        NonTransactionalAuditEvent event = NonTransactionalAuditEvent.of(
                AuditEventType.ACCESS_DENIED, 7L, false, "device", Map.of("reason", "scope"));

        CorrelationIdContext.clear();

        assertThat(event.getCorrelationId()).isEqualTo(CORRELATION_ID);
    }

    @Test
    void serverAuditListenerPersistsCapturedCorrelationIdAfterRequestContextEnds() {
        AuditEventCommandRepository repository = mock(AuditEventCommandRepository.class);
        ServerAuditEventListener listener = new ServerAuditEventListener(repository, JsonMapper.builder().build());
        CorrelationIdContext.set(CORRELATION_ID);
        TransactionalAuditEvent event = TransactionalAuditEvent.of(
                AuditEventType.LOGIN_SUCCESS, 7L, false, "device", null);
        CorrelationIdContext.clear();

        listener.onTransactionalEvent(event);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCorrelationId()).isEqualTo(CORRELATION_ID);
    }
}
