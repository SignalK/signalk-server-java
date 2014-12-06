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

import static org.junit.Assert.assertEquals;
import nz.co.fortytwo.signalk.processor.WindProcessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WindProcessorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTrueWindDir() {
		WindProcessor wp = new WindProcessor();
		// test 0 wind, 0deg, 0spd
		double [] windcalc = wp.calcTrueWindDirection(0, 0, 0);
		assertEquals(0.0,windcalc[1], 1.0);
		assertEquals(0.0, windcalc[0], 0.1);

		// test 10 wind, 90deg, 0spd
		windcalc = wp.calcTrueWindDirection(10, 90, 0);
		assertEquals(90.0, windcalc[1], 1.0);
		assertEquals(10.0, windcalc[0], 0.1);
		
		// test 10 wind, 900deg, 10spd = 135deg 14.14
		windcalc = wp.calcTrueWindDirection(10, 90, 10);
		assertEquals(135.0, windcalc[1], 1.0);
		assertEquals(14.14, windcalc[0], 0.1);
		
		// test 10 wind, 270deg, 10spd = 360-135, 14.14
		windcalc = wp.calcTrueWindDirection(10, 270, 10);
		assertEquals(225.0, windcalc[1], 1.0);
		assertEquals(14.14, windcalc[0], 0.1);
		
		// test .3 wind, 80deg, 0.5spd = 146, 0.9
		windcalc = wp.calcTrueWindDirection(.3, 80, .5);
		assertEquals(146.0, windcalc[1], 1.0);
		assertEquals(0.5, windcalc[0], 0.1);
		
		// test 10 wind, -90deg, 6.5spd = 146, 0.9
		windcalc = wp.calcTrueWindDirection(10, 270, 6.5);
		assertEquals(360-123.0, windcalc[1], 1.0);
		assertEquals(11.9, windcalc[0], 0.1);

	}
	

}
