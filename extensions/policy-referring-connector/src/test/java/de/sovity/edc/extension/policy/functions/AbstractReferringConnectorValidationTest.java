/*
 * Copyright (c) 2022 Mercedes-Benz Tech Innovation GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - adaptation and modifications
 *
 */

package de.sovity.edc.extension.policy.functions;

import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.spi.agent.ParticipantAgent;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

class AbstractReferringConnectorValidationTest {

    private AbstractReferringConnectorValidation validation;

    // mocks
    private Monitor monitor;
    private PolicyContext policyContext;
    private ParticipantAgent participantAgent;

    @BeforeEach
    void beforeEach() {
        this.monitor = Mockito.mock(Monitor.class);
        this.policyContext = Mockito.mock(PolicyContext.class);
        this.participantAgent = Mockito.mock(ParticipantAgent.class);

        Mockito.when(policyContext.getParticipantAgent()).thenReturn(participantAgent);

        validation = new AbstractReferringConnectorValidation(monitor) {};
    }

    @ParameterizedTest
    @EnumSource(Operator.class)
    void testFailsOnUnsupportedOperations(Operator operator) {

        if (operator == Operator.EQ) { // only allowed operator
            return;
        }

        // prepare
        prepareContextProblems(null);
        prepareReferringConnectorClaim("http://example.org");

        // invoke & assert
        Assertions.assertFalse(validation.evaluate(operator, "foo", policyContext));
    }

    @Test
    void testFailsOnUnsupportedRightValue() {

        // prepare
        prepareContextProblems(null);
        prepareReferringConnectorClaim("http://example.org");

        // invoke & assert
        Assertions.assertFalse(validation.evaluate(Operator.EQ, 1, policyContext));
    }

    @Test
    void testValidationFailsWhenClaimMissing() {

        // prepare
        prepareContextProblems(null);

        // invoke
        final boolean isValid = validation.evaluate(Operator.EQ, "http://example.org", policyContext);

        // assert
        Assertions.assertFalse(isValid);
    }

    @Test
    void testValidationWhenClaimContainsValue() {

        // prepare
        prepareContextProblems(null);

        // prepare equals
        prepareReferringConnectorClaim("http://example.org");
        final boolean isEqualsTrue = validation.evaluate(Operator.EQ, "http://example.org", policyContext);

        // prepare contains
        prepareReferringConnectorClaim("http://example.com/http://example.org");
        final boolean isContainedTrue = validation.evaluate(Operator.EQ, "http://example.org", policyContext);

        // assert
        Assertions.assertTrue(isEqualsTrue);
        Assertions.assertFalse(isContainedTrue);
    }

    @Test
    void testValidationWhenParticipantHasProblems() {

        // prepare
        prepareContextProblems(Collections.singletonList("big problem"));
        prepareReferringConnectorClaim("http://example.org");

        // invoke
        final boolean isValid = validation.evaluate(Operator.EQ, "http://example.org", policyContext);

        // Mockito.verify(monitor.debug(Mockito.anyString());
        Assertions.assertFalse(isValid);
    }

    @Test
    void testValidationWhenSingleParticipantIsValid() {

        // prepare
        prepareContextProblems(null);
        prepareReferringConnectorClaim("http://example.org");

        // invoke
        final boolean isContainedTrue = validation.evaluate(Operator.EQ, "http://example.org", policyContext);

        // Mockito.verify(monitor.debug(Mockito.anyString());
        Assertions.assertTrue(isContainedTrue);
    }

    // In the past it was possible to use the 'IN' constraint
    // with multiple referringConnector as
    // a list. This is no longer supported.
    // The EDC must now always decline this kind of referringConnector format.
    @Test
    void testValidationForMultipleParticipants() {

        // prepare
        prepareContextProblems(null);
        prepareReferringConnectorClaim("http://example.org");

        // invoke & verify
        Assertions.assertFalse(validation.evaluate(Operator.IN, List.of("http://example.org", "http://example.com"), policyContext));
        Assertions.assertFalse(validation.evaluate(Operator.IN, List.of(1, "http://example.org"), policyContext));
        Assertions.assertFalse(validation.evaluate(Operator.IN, List.of("http://example.org", "http://example.org"), policyContext));
    }

    private void prepareContextProblems(List<String> problems) {
        Mockito.when(policyContext.getProblems()).thenReturn(problems);

        if (problems == null || problems.isEmpty()) {
            Mockito.when(policyContext.hasProblems()).thenReturn(false);
        } else {
            Mockito.when(policyContext.hasProblems()).thenReturn(true);
        }
    }

    private void prepareReferringConnectorClaim(String referringConnector) {
        Mockito.when(participantAgent.getClaims())
                .thenReturn(Collections.singletonMap("referringConnector", referringConnector));
    }
}