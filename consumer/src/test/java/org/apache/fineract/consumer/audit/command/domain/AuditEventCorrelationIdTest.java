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
package org.apache.fineract.consumer.audit.command.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.apache.fineract.consumer.infrastructure.audit.data.AuditEventType;
import org.junit.jupiter.api.Test;

class AuditEventCorrelationIdTest {

    private static final String CORRELATION_ID = "corr-entity-123";

    @Test
    void serverFactoryStoresCorrelationId() {
        AuditEvent event = AuditEvent.forServerEvent(
                UUID.randomUUID(), AuditEventType.LOGIN_SUCCESS, AuditEventType.LOGIN_SUCCESS.getSeverity(),
                7L, false, "device", CORRELATION_ID, null);

        assertThat(event.getCorrelationId()).isEqualTo(CORRELATION_ID);
    }

    @Test
    void clientFactoryStoresCorrelationId() {
        AuditEvent event = AuditEvent.forClientEvent(
                UUID.randomUUID(), AuditEventType.NAVIGATION, AuditEventType.NAVIGATION.getSeverity(),
                7L, "device", CORRELATION_ID, Instant.now(), null);

        assertThat(event.getCorrelationId()).isEqualTo(CORRELATION_ID);
    }
}
