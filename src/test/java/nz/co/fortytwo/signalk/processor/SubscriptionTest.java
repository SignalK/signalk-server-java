/*
 *
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 *
 * This file is part of the signalk-server-java project
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package nz.co.fortytwo.signalk.processor;

import static org.junit.Assert.*;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.util.JsonConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SubscriptionTest {

	@BeforeClass
	public static void setClass() throws Exception {
		Util.getConfig();
		Util.setSelf("motu");
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldBeSubscribed() {
		Subscription sub = new Subscription("wsSession", "vessels.self.navigation", 10, 1000, JsonConstants.FORMAT_FULL, JsonConstants.POLICY_FIXED);
		assertTrue(sub.isSubscribed("vessels.motu.navigation.courseOverGroundTrue"));
	}
	
	@Test
	public void shouldBeWildcardSubscribed() {
		Subscription sub = new Subscription("wsSession", "vessels.*.navigation", 10, 1000, JsonConstants.FORMAT_FULL, JsonConstants.POLICY_FIXED);
		assertTrue(sub.isSubscribed("vessels.motu.navigation.courseOverGroundTrue"));
		assertTrue(sub.isSubscribed("vessels.notMotu.navigation.courseOverGroundTrue"));
	}
	
	@Test
	public void shouldBeCharWildcardSubscribed() {
		Subscription sub = new Subscription("wsSession", "vessels.motu?.navigation", 10, 1000, JsonConstants.FORMAT_FULL, JsonConstants.POLICY_FIXED);
		assertFalse(sub.isSubscribed("vessels.motu.navigation.courseOverGroundTrue"));
		assertFalse(sub.isSubscribed("vessels.Motu.navigation.courseOverGroundTrue"));
		assertTrue(sub.isSubscribed("vessels.motux.navigation.courseOverGroundTrue"));
	}
	
	@Test
	public void shouldBePartialWildcardSubscribed() {
		Subscription sub = new Subscription("wsSession", "vessels.*.navigation.course*True", 10, 1000, JsonConstants.FORMAT_FULL, JsonConstants.POLICY_FIXED);
		assertTrue(sub.isSubscribed("vessels.motu.navigation.courseOverGroundTrue"));
		assertTrue(sub.isSubscribed("vessels.notMotu.navigation.courseOverWaterTrue"));
		assertFalse(sub.isSubscribed("vessels.notMotu.navigation.courseOverWaterApparent"));
	}
	
	@Test
	public void shouldNotBeSubscribed() {
		Subscription sub = new Subscription("wsSession", "vessels.self.navigation", 10, 1000, JsonConstants.FORMAT_FULL, JsonConstants.POLICY_FIXED);
		
		assertFalse(sub.isSubscribed("vessels.notMotu.navigation.courseOverGroundTrue"));
	}
	@Test
	public void shouldNotBeWildcardSubscribed() {
		Subscription sub = new Subscription("wsSession", "vessels.*.navigation", 10, 1000, JsonConstants.FORMAT_FULL, JsonConstants.POLICY_FIXED);
		assertTrue(sub.isSubscribed("vessels.notMotu.navigation.courseOverGroundTrue"));
		assertFalse(sub.isSubscribed("vessels.notMotu.environment.wind"));
	}
	@Test
	public void shouldNotBePartialWildcardSubscribed() {
		Subscription sub = new Subscription("wsSession", "vessels.*.navigation.course*", 10, 1000, JsonConstants.FORMAT_FULL, JsonConstants.POLICY_FIXED);
		assertTrue(sub.isSubscribed("vessels.notMotu.navigation.courseOverGroundTrue"));
		assertFalse(sub.isSubscribed("vessels.notMotu.navigation.position"));
	}
	
	@Test
	public void shouldNotBeSubscribedToParents() {
		Subscription sub = new Subscription("wsSession", "vessels.*.navigation.course*", 10, 1000, JsonConstants.FORMAT_FULL, JsonConstants.POLICY_FIXED);
		assertFalse(sub.isSubscribed("vessels.notMotu.navigation"));
		assertFalse(sub.isSubscribed("vessels.notMotu"));
	}

}
