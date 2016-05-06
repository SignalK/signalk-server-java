/*
 * 
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
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
package nz.co.fortytwo.signalk.processor;

import java.util.NavigableSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.CamelContextFactory;
import nz.co.fortytwo.signalk.server.RouteManagerFactory;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.server.SubscriptionManager;
import nz.co.fortytwo.signalk.server.SubscriptionManagerFactory;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.SignalKConstants.POLICY_IDEAL;
import static nz.co.fortytwo.signalk.util.SignalKConstants.dot;
import static nz.co.fortytwo.signalk.util.SignalKConstants.env_wind_angleApparent;
import static nz.co.fortytwo.signalk.util.SignalKConstants.nav_courseOverGroundTrue;
import static nz.co.fortytwo.signalk.util.SignalKConstants.vessels;
import nz.co.fortytwo.signalk.util.TestHelper;
import nz.co.fortytwo.signalk.util.Util;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

public class FullExportProcessorTest {

    private static Logger logger = LogManager.getLogger(FullExportProcessorTest.class);

    private static SubscriptionManager subscriptionManager;
    private static SignalKModel model;

    private NavigableSet<String> self_nav;
    private NavigableSet<String> self_env;
    private NavigableSet<String> multiple_keys;
    private NavigableSet<String> other_env;

    @BeforeClass
    public static void setClass() throws Exception {
        Util.getConfig();
        Util.setSelf("motu");

        RouteManagerFactory.getMotuTestInstance().getContext();
        subscriptionManager = SubscriptionManagerFactory.getInstance();

        model = SignalKModelFactory.getMotuTestInstance();
        model.putAll(TestHelper.getBasicModel().getFullData());
        model.putAll(TestHelper.getOtherModel().getFullData());
    }

    @Before
    public void setUp() throws Exception {
        self_nav = getKeysForEvent(SignalKConstants.self, nav_courseOverGroundTrue);
        self_env = getKeysForEvent(SignalKConstants.self, env_wind_angleApparent);
        other_env = getKeysForEvent("other", env_wind_angleApparent);

        multiple_keys = new ConcurrentSkipListSet<>();
        multiple_keys.addAll(self_nav);
        multiple_keys.addAll(other_env);
    }

    private NavigableSet<String> getKeysForEvent(String mmsi, String ref) {
        return model.getTree(vessels + dot + mmsi + dot + ref);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldEmitIfMatchesWithIdealPolicy() throws Exception {
        testScenario("vessels.self.navigation", SignalKConstants.POLICY_IDEAL, 1, self_nav);
    }

    @Test
    public void shouldEmitIfMatchesWithInstantPolicy() throws Exception {
        testScenario("vessels.self.navigation", SignalKConstants.POLICY_INSTANT, 3, self_nav);
    }

    @Test
    public void shouldNotEmitIfMatchesWithFixedPolicy() throws Exception {
        testScenario("vessels.self.navigation", SignalKConstants.POLICY_FIXED, 0, self_nav);
    }

    @Test
    public void shouldNotEmitIfNoMatchWithIdealPolicy() throws Exception {
        testScenario("vessels.self.invalid", SignalKConstants.POLICY_IDEAL, 0, self_nav);
    }

    @Test
    public void shouldEmitIfPatternMatchesWithIdealPolicy() throws Exception {
        testScenario("vessels.*.environment", SignalKConstants.POLICY_IDEAL, 1, self_env);
        testScenario("vessels.*.environment", SignalKConstants.POLICY_IDEAL, 1, other_env);
    }

    @Test
    public void shouldEmitIfPatternPartiallyMatches() throws Exception {
        testScenario("vessels.*.environment", SignalKConstants.POLICY_IDEAL, 1, multiple_keys);
    }

    @Test
    public void shouldEmitIfPatternFullyMatches() throws Exception {
        testScenario("vessels.*", SignalKConstants.POLICY_IDEAL, 1, multiple_keys);
    }

    private void testScenario(String subKey, String policy, int expectedCount, NavigableSet<String> keys) throws Exception {

        CamelContext ctx = CamelContextFactory.getInstance();
        MockEndpoint resultEndpoint = (MockEndpoint) ctx.getEndpoint("mock:resultEnd");

        String session = UUID.randomUUID().toString();
        Subscription sub = new Subscription(session, subKey, 10, 1000, FORMAT_DELTA, policy);
        subscriptionManager.add("ses" + session, session, ConfigConstants.OUTPUT_WS, "127.0.0.1", "127.0.0.1");
        subscriptionManager.addSubscription(sub);
        try {
            FullExportProcessor processor = new FullExportProcessor(session);
            ProducerTemplate exportProducer = new DefaultProducerTemplate(ctx);
            exportProducer.setDefaultEndpointUri("mock:resultEnd");
            exportProducer.start();
            processor.outProducer = exportProducer;

            resultEndpoint.expectedMessageCount(expectedCount);

            for (String key : keys) {
                processor.recordEvent(new PathEvent(key, 0, nz.co.fortytwo.signalk.model.event.PathEvent.EventType.ADD));
                logger.debug("Posted path event:" + key);
            }

            // Sleep to allow for minPeriod.
            if (POLICY_IDEAL.equals(policy)) {
                Thread.sleep(100L);
            }

            resultEndpoint.assertIsSatisfied();
        } finally {
            subscriptionManager.removeSubscription(sub);
            subscriptionManager.removeWsSession(session);
            resultEndpoint.reset();
        }
    }

}
