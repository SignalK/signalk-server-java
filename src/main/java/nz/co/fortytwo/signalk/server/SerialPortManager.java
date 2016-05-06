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

package nz.co.fortytwo.signalk.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

/**
 * A manager to monitor the USB tty ports. It dynamically adds/removes
 * ports as the USB devices are added/removed
 * 
 * @author robert
 * 
 */
public class SerialPortManager implements Runnable, Processor {

	private static Logger logger = LogManager.getLogger(SerialPortManager.class);

	private List<SerialPortReader> serialPortList = new CopyOnWriteArrayList<SerialPortReader>();

	private boolean running = true;

	@SuppressWarnings("static-access")
	public void run() {
		// not running, start now.
		ProducerTemplate producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri(RouteManager.SEDA_INPUT);
		
		while (running) {
			// remove any stopped readers
			List<SerialPortReader> tmpPortList = new ArrayList<SerialPortReader>();
			for (SerialPortReader reader : serialPortList) {
				if (!reader.isRunning()) {
					if(logger.isDebugEnabled())logger.debug("Comm port " + reader.getPortName() + " finished and marked for removal");
					tmpPortList.add(reader);
				}
				if(logger.isDebugEnabled())logger.debug("Comm port " + reader.getPortName() + " currently running");
			}
			serialPortList.removeAll(tmpPortList);
			//json array
			String portStr ="[\"/dev/ttyUSB0\",\"/dev/ttyUSB1\",\"/dev/ttyUSB2\"]";
			portStr = Util.getConfigProperty(ConfigConstants.SERIAL_PORTS);
			portStr=portStr.replace("[", "");
			portStr=portStr.replace("]", "");
			portStr=portStr.replace("\"", "");
			//now we have just comma delim text.
			String[] ports = portStr.split(",");
			for (String port:ports) {
				boolean portOk = false;
				
				try {
					//this doesnt work  on windozy
					if(!SystemUtils.IS_OS_WINDOWS){
						File portFile = new File(port);
						if (!portFile.exists()){
							if(logger.isDebugEnabled())logger.debug("Comm port "+port+" doesnt exist");
							continue;
						}
					}
					for (SerialPortReader reader : serialPortList) {
						if (StringUtils.equals(port, reader.getPortName())) {
							// its already up and running
							portOk = true;
						}
					}
					// if its running, ignore
					if (portOk){
						if(logger.isDebugEnabled())logger.debug("Comm port " + port + " found already connected");
						continue;
					}

					
					SerialPortReader serial = new SerialPortReader();
					serial.setProducer(producer);
					//default 38400, then freeboard.cfg default, then freeboard.cfg per port
					int baudRate = Util.getConfigPropertyInt(ConfigConstants.SERIAL_PORT_BAUD);
					//get port name
					String portName = port;
					if(port.indexOf("/")>0){
						portName=port.substring(port.lastIndexOf("/")+1);
					}
					if(Util.getConfigPropertyInt(ConfigConstants.SERIAL_PORT_BAUD+"."+portName)!=null){
						baudRate = Util.getConfigPropertyInt(ConfigConstants.SERIAL_PORT_BAUD+"."+portName);
					}
					if(logger.isDebugEnabled())logger.debug("Comm port "+ConfigConstants.SERIAL_PORT_BAUD+"."+portName+" override="+Util.getConfigProperty(ConfigConstants.SERIAL_PORT_BAUD+"."+portName));
					if(logger.isDebugEnabled())logger.debug("Comm port " + port + " found and connecting at "+baudRate+"...");
					serial.connect(port, baudRate);
					if(logger.isDebugEnabled())logger.info("Comm port " + port + " found and connected");
					serialPortList.add(serial);
				} catch (NullPointerException np) {
					logger.error("Comm port " + port + " was null, probably not found, or nothing connected");
				} catch (purejavacomm.NoSuchPortException nsp) {
					logger.error("Comm port " + port + " not found, or nothing connected");
				} catch (Exception e) {
					logger.error("Port " + port + " failed", e);
				}
			}
			// delay for 30 secs, we dont want to burn up CPU for nothing
			try {
				Thread.currentThread().sleep(10 * 1000);
			} catch (InterruptedException ie) {
			}
		}
	}

	/**
	 * When the serial port is used to read from the arduino this must be called to shut
	 * down the readers, which are in their own threads.
	 */
	public void stopSerial() {

		for (SerialPortReader serial : serialPortList) {
			if (serial != null) {
				serial.setRunning(false);
			}
		}
		running = false;

	}

	public void process(Exchange exchange) throws Exception {
		for (SerialPortReader serial : serialPortList) {
			if (serial != null) {
				serial.process(exchange);
			}
		}
		
	}



}
