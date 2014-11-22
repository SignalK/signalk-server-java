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
package nz.co.fortytwo.signalk.server.util;

/*                     <p><center>PUBLIC DOMAIN NOTICE</center></p><p>
This program was prepared by Los Alamos National Security, LLC 
at Los Alamos National Laboratory (LANL) under contract No. 
DE-AC52-06NA25396 with the U.S. Department of Energy (DOE). 
All rights in the program are reserved by the DOE and 
Los Alamos National Security, LLC.  Permission is granted to the 
public to copy and use this software without charge, 
provided that this Notice and any statement of authorship are 
reproduced on all copies.  Neither the U.S. Government nor LANS 
makes any warranty, express or implied, or assumes any liability 
or responsibility for the use of this software.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.junit.Test;


/**<p>
 *  Test values from
 *  <a href ="http://www.ngdc.noaa.gov/geomag/WMM/soft.shtml"> the National GeoPhysical Data Center.</a>.
 *  Click on the WMM2010testvalues.pdf link.
 *  </p><p>
 *  You have to run this test twice. Once with the WMM.COF
 *  file present, and then with it missing. Otherwise the
 *  setCoeff method is never tested.</p>
 *  
 * @version 1.0 Apr 14, 2006
 * @author John St. Ledger
 * @version 1.1 Jan 28, 2009
 * <p>Added 2006 test values.</p>
 * @version 1.2 Jan 5, 2010
 * <p>Updated with the test values for the 2010 WMM.COF coefficients. From page 18 of
 * <i>The US/UK World Magnetic Model for 2010-2015, Preliminary online version containing
 * final WMM2010 model coefficients</i></p>
 */
public class TSAGeoMagTest 
{
    TSAGeoMag magModel = new TSAGeoMag();
    /**
     * Test method for {@link d3.env.TSAGeoMag#getDeclination(double, double, double, double)}
     */
    @Test public final void getDeclination() 
    {
        assertEquals(-6.13, magModel.getDeclination(80, 0, 2010, 0) , 1.0E-02);
        assertEquals(0.97, magModel.getDeclination(0, 120, 2010, 0) , 1.0E-02);
        assertEquals(70.21, magModel.getDeclination(-80, 240, 2010, 0) , 1.0E-02);
        assertEquals(-6.57, magModel.getDeclination(80, 0, 2010, 100) , 1.0E-02);
        assertEquals(0.94, magModel.getDeclination(0, 120, 2010, 100) , 1.0E-02);
        assertEquals(69.62, magModel.getDeclination(-80, 2400, 2010, 100) , 1.0E-02);
        
        assertEquals(-5.21, magModel.getDeclination(80, 0, 2012.5, 0) , 1.0E-02);
        assertEquals(0.88, magModel.getDeclination(0, 120, 2012.5, 0) , 1.0E-02);
        assertEquals(70.04, magModel.getDeclination(-80, 240, 2012.5, 0) , 1.0E-02);
        assertEquals(-5.63, magModel.getDeclination(80, 0, 2012.5, 100) , 1.0E-02);
        assertEquals(0.86, magModel.getDeclination(0, 120, 2012.5, 100) , 1.0E-02);
        assertEquals(69.45, magModel.getDeclination(-80, 2400, 2012.5, 100) , 1.0E-02);
        
        assertEquals(-5.21, magModel.getDeclination(80, 0) , 1.0E-02);
        assertEquals(0.88, magModel.getDeclination(0, 120) , 1.0E-02);
        assertEquals(70.04, magModel.getDeclination(-80, 240) , 1.0E-02);
    }
    
    /**
     * Test method for {@link d3.env.TSAGeoMag#getDipAngle(double, double, double, double)}
     */
    @Test public final void getDipAngle() 
    {
        assertEquals(82.98, magModel.getDipAngle(80, 0, 2010, 0) , 1.0E-02);
        assertEquals(-16.50, magModel.getDipAngle(0, 120, 2010, 0) , 1.0E-02);
        assertEquals(-72.62, magModel.getDipAngle(-80, 240, 2010, 0) , 1.0E-02);
        assertEquals(83.04, magModel.getDipAngle(80, 0, 2010, 100) , 1.0E-02);
        assertEquals(-16.62, magModel.getDipAngle(0, 120, 2010, 100) , 1.0E-02);
        assertEquals(-72.79, magModel.getDipAngle(-80, 2400, 2010, 100) , 1.0E-02);
        
        assertEquals(83.00, magModel.getDipAngle(80, 0, 2012.5, 0) , 1.0E-02);
        assertEquals(-16.31, magModel.getDipAngle(0, 120, 2012.5, 0) , 1.0E-02);
        assertEquals(-72.53, magModel.getDipAngle(-80, 240, 2012.5, 0) , 1.0E-02);
        assertEquals(83.05, magModel.getDipAngle(80, 0, 2012.5, 100) , 1.0E-02);
        assertEquals(-16.43, magModel.getDipAngle(0, 120, 2012.5, 100) , 1.0E-02);
        assertEquals(-72.70, magModel.getDipAngle(-80, 2400, 2012.5, 100) , 1.0E-02);
        
        assertEquals(83.00, magModel.getDipAngle(80, 0) , 1.0E-02);
        assertEquals(-16.31, magModel.getDipAngle(0, 120) , 1.0E-02);
        assertEquals(-72.53, magModel.getDipAngle(-80, 240) , 1.0E-02);
    }
    /**
     * Test method for {@link d3.env.TSAGeoMag#getHorizontalIntensity(double, double, double, double)} in nT
     * and {@link d3.env.TSAGeoMag#getHorizontalIntensity(double, double)} in nT
     */
    @Test public final void getHorizontalIntensity() 
    {
        assertEquals(6687.8, magModel.getHorizontalIntensity(80, 0, 2010, 0) , 1.0E-01);
        assertEquals(39434.5, magModel.getHorizontalIntensity(0, 120, 2010, 0) , 1.0E-01);
        assertEquals(16714.0, magModel.getHorizontalIntensity(-80, 240, 2010, 0) , 1.0E-01);
        assertEquals(6374.0, magModel.getHorizontalIntensity(80, 0, 2010, 100) , 1.0E-01);
        assertEquals(37457.0, magModel.getHorizontalIntensity(0, 120, 2010, 100) , 1.0E-01);
        assertEquals(15748.6, magModel.getHorizontalIntensity(-80, 2400, 2010, 100) , 1.0E-01);
        
        assertEquals(6685.5, magModel.getHorizontalIntensity(80, 0, 2012.5, 0) , 1.0E-01);
        assertEquals(39428.6, magModel.getHorizontalIntensity(0, 120, 2012.5, 0) , 1.0E-01);
        assertEquals(16737.2, magModel.getHorizontalIntensity(-80, 240, 2012.5, 0) , 1.0E-01);
        assertEquals(6371.6, magModel.getHorizontalIntensity(80, 0, 2012.5, 100) , 1.0E-01);
        assertEquals(37452.2, magModel.getHorizontalIntensity(0, 120, 2012.5, 100) , 1.0E-01);
        assertEquals(15768.9, magModel.getHorizontalIntensity(-80, 2400, 2012.5, 100) , 1.0E-01);
        
        assertEquals(6685.5, magModel.getHorizontalIntensity(80, 0) , 1.0E-01);
        assertEquals(39428.6, magModel.getHorizontalIntensity(0, 120) , 1.0E-01);
        assertEquals(16737.2, magModel.getHorizontalIntensity(-80, 240) , 1.0E-01);
    }
    /**
     * Test method for d3.env.TSAGeoMag.getEastIntensity() in nT
     */
    @Test public final void getEastIntensity() 
    {
        assertEquals(6649.5, magModel.getEastIntensity(80, 0, 2010, 0) , 1.0E-01);
        assertEquals(39428.8, magModel.getEastIntensity(0, 120, 2010, 0) , 1.0E-01);
        assertEquals(5657.7, magModel.getEastIntensity(-80, 240, 2010, 0) , 1.0E-01);
        assertEquals(6332.2, magModel.getEastIntensity(80, 0, 2010, 100) , 1.0E-01);
        assertEquals(37452.0, magModel.getEastIntensity(0, 120, 2010, 100) , 1.0E-01);
        assertEquals(5484.3, magModel.getEastIntensity(-80, 2400, 2010, 100) , 1.0E-01);
        
        assertEquals(6658.0, magModel.getEastIntensity(80, 0, 2012.5, 0) , 1.0E-01);
        assertEquals(39423.9, magModel.getEastIntensity(0, 120, 2012.5, 0) , 1.0E-01);
        assertEquals(5713.6, magModel.getEastIntensity(-80, 240, 2012.5, 0) , 1.0E-01);
        assertEquals(6340.9, magModel.getEastIntensity(80, 0, 2012.5, 100) , 1.0E-01);
        assertEquals(37448.1, magModel.getEastIntensity(0, 120, 2012.5, 100) , 1.0E-01);
        assertEquals(5535.5, magModel.getEastIntensity(-80, 2400, 2012.5, 100) , 1.0E-01);
        
        assertEquals(6658.0, magModel.getEastIntensity(80, 0) , 1.0E-01);
        assertEquals(39423.9, magModel.getEastIntensity(0, 120) , 1.0E-01);
        assertEquals(5713.6, magModel.getEastIntensity(-80, 240) , 1.0E-01);
    }
    /**
     * Test method for d3.env.TSAGeoMag.getNorthIntensity() in nT
     */
    @Test public final void getNorthIntensity() 
    {
        assertEquals(-714.6, magModel.getNorthIntensity(80, 0, 2010, 0) , 1.0E-01);
        assertEquals(664.9, magModel.getNorthIntensity(0, 120, 2010, 0) , 1.0E-01);
        assertEquals(15727.3, magModel.getNorthIntensity(-80, 240, 2010, 0) , 1.0E-01);
        assertEquals(-729.1, magModel.getNorthIntensity(80, 0, 2010, 100) , 1.0E-01);
        assertEquals(611.9, magModel.getNorthIntensity(0, 120, 2010, 100) , 1.0E-01);
        assertEquals(14762.8, magModel.getNorthIntensity(-80, 2400, 2010, 100) , 1.0E-01);
        
        assertEquals(-606.7, magModel.getNorthIntensity(80, 0, 2012.5, 0) , 1.0E-01);
        assertEquals(608.1, magModel.getNorthIntensity(0, 120, 2012.5, 0) , 1.0E-01);
        assertEquals(15731.8, magModel.getNorthIntensity(-80, 240, 2012.5, 0) , 1.0E-01);
        assertEquals(-625.1, magModel.getNorthIntensity(80, 0, 2012.5, 100) , 1.0E-01);
        assertEquals(559.7, magModel.getNorthIntensity(0, 120, 2012.5, 100) , 1.0E-01);
        assertEquals(14765.4, magModel.getNorthIntensity(-80, 2400, 2012.5, 100) , 1.0E-01);
        
        assertEquals(-606.7, magModel.getNorthIntensity(80, 0) , 1.0E-01);
        assertEquals(608.1, magModel.getNorthIntensity(0, 120) , 1.0E-01);
        assertEquals(15731.8, magModel.getNorthIntensity(-80, 240) , 1.0E-01);
    }
    /**
     * Test method for d3.env.TSAGeoMag.getVerticalIntensity()
     */
    @Test public final void getVerticalIntensity() 
    {
        assertEquals(54346.2, magModel.getVerticalIntensity(80, 0, 2010, 0) , 1.0E-01);
        assertEquals(-11683.8, magModel.getVerticalIntensity(0, 120, 2010, 0) , 1.0E-01);
        assertEquals(-53407.5, magModel.getVerticalIntensity(-80, 240, 2010, 0) , 1.0E-01);
        assertEquals(52194.9, magModel.getVerticalIntensity(80, 0, 2010, 100) , 1.0E-01);
        assertEquals(-11180.8, magModel.getVerticalIntensity(0, 120, 2010, 100) , 1.0E-01);
        assertEquals(-50834.8, magModel.getVerticalIntensity(-80, 2400, 2010, 100) , 1.0E-01);
        
        assertEquals(54420.4, magModel.getVerticalIntensity(80, 0, 2012.5, 0) , 1.0E-01);
        assertEquals(-11540.5, magModel.getVerticalIntensity(0, 120, 2012.5, 0) , 1.0E-01);
        assertEquals(-53184.3, magModel.getVerticalIntensity(-80, 240, 2012.5, 0) , 1.0E-01);
        assertEquals(52261.9, magModel.getVerticalIntensity(80, 0, 2012.5, 100) , 1.0E-01);
        assertEquals(-11044.2, magModel.getVerticalIntensity(0, 120, 2012.5, 100) , 1.0E-01);
        assertEquals(-50625.9, magModel.getVerticalIntensity(-80, 2400, 2012.5, 100) , 1.0E-01);
        
        assertEquals(54420.4, magModel.getVerticalIntensity(80, 0) , 1.0E-01);
        assertEquals(-11540.5, magModel.getVerticalIntensity(0, 120) , 1.0E-01);
        assertEquals(-53184.3, magModel.getVerticalIntensity(-80, 240) , 1.0E-01);
    }
    /**
     * Test method for d3.env.TSAGeoMag.getIntensity()
     */
    @Test public final void getIntensity() 
    {
        assertEquals(54756.2, magModel.getIntensity(80, 0, 2010, 0) , 1.0E-01);
        assertEquals(41128.9, magModel.getIntensity(0, 120, 2010, 0) , 1.0E-01);
        assertEquals(55961.8, magModel.getIntensity(-80, 240, 2010, 0) , 1.0E-01);
        assertEquals(52582.6, magModel.getIntensity(80, 0, 2010, 100) , 1.0E-01);
        assertEquals(39090.1, magModel.getIntensity(0, 120, 2010, 100) , 1.0E-01);
        assertEquals(53218.3, magModel.getIntensity(-80, 2400, 2010, 100) , 1.0E-01);
        
        assertEquals(54829.5, magModel.getIntensity(80, 0, 2012.5, 0) , 1.0E-01);
        assertEquals(41082.8, magModel.getIntensity(0, 120, 2012.5, 0) , 1.0E-01);
        assertEquals(55755.7, magModel.getIntensity(-80, 240, 2012.5, 0) , 1.0E-01);
        assertEquals(52648.9, magModel.getIntensity(80, 0, 2012.5, 100) , 1.0E-01);
        assertEquals(39046.7, magModel.getIntensity(0, 120, 2012.5, 100) , 1.0E-01);
        assertEquals(53024.9, magModel.getIntensity(-80, 2400, 2012.5, 100) , 1.0E-01);
        
        assertEquals(54829.5, magModel.getIntensity(80, 0) , 1.0E-01);
        assertEquals(41082.8, magModel.getIntensity(0, 120) , 1.0E-01);
        assertEquals(55755.7, magModel.getIntensity(-80, 240) , 1.0E-01);
    }
    
    /**
     *  test method for {@link d3.env.TSAGeoMag#decimalYear(GregorianCalendar)}
     */
    @Test
    public final void decimalYear()
    {
	TSAGeoMag mag = new TSAGeoMag();
	
	GregorianCalendar cal = new GregorianCalendar(2010, 0, 0);
	assertEquals(2010.0, mag.decimalYear(cal), 0.0);
	
	GregorianCalendar cal2 = new GregorianCalendar(2012, 6, 1);  // the full day of July 1, 0 hours into 2 July
	assertTrue(cal2.isLeapYear(2012));
	assertEquals(2012.5, mag.decimalYear(cal2), 0.0);
    }
}
