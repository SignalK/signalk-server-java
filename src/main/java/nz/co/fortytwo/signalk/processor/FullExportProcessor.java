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

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.server.Subscription;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import static nz.co.fortytwo.signalk.util.SignalKConstants.FORMAT_DELTA;
import static nz.co.fortytwo.signalk.util.SignalKConstants.POLICY_FIXED;
import static nz.co.fortytwo.signalk.util.SignalKConstants.SIGNALK_FORMAT;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.log4j.Logger;

/**
 * Exports the signalkModel as a json object
 *
 * @author robert
 */
public class FullExportProcessor extends SignalkProcessor implements Processor {

    static Logger logger = Logger.getLogger(FullExportProcessor.class);
    protected String wsSession = null;

    public FullExportProcessor(String wsSession) {
        super();
        this.wsSession = wsSession;
        signalkModel.getEventBus().register(this);

    }

    public void process(Exchange exchange) throws Exception {

        try {
            if (logger.isDebugEnabled())
                logger.info("process  subs for " + exchange.getFromRouteId() + " as delta? " + isDelta(exchange.getFromRouteId()));
            // get the accumulated delta nodes.
            exchange.getIn().setBody(createTree(exchange.getFromRouteId()));
            setHeaders(exchange);
            if (logger.isDebugEnabled()) {
                logger.debug("Headers set to :" + exchange.getIn().getHeaders());
                logger.debug("Body set to :" + exchange.getIn().getBody());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    private void setHeaders(Exchange exchange) {
        for (Subscription sub : manager.getSubscriptions(wsSession)) {
            if (sub == null || !sub.isActive() || !exchange.getFromRouteId().equals(sub.getRouteId()))
                continue;
            exchange.getIn().setHeader(SIGNALK_FORMAT, sub.getFormat());
            if (sub.getDestination() != null) {
                exchange.getIn().setHeader(ConfigConstants.DESTINATION, sub.getDestination());

            }
            exchange.getIn().setHeader(ConfigConstants.OUTPUT_TYPE, manager.getOutputType(sub.getWsSession()));
            exchange.getIn().setHeader(WebsocketConstants.CONNECTION_KEY, sub.getWsSession());

        }
    }

    private boolean isDelta(String routeId) {
        for (Subscription sub : manager.getSubscriptions(wsSession)) {
            if (sub != null && sub.isActive() && routeId.equals(sub.getRouteId()) && FORMAT_DELTA.equals(sub.getFormat())) {
                return true;
            }
        }
        return false;
    }

    private SignalKModel createTree(String routeId) {
        SignalKModel temp = SignalKModelFactory.getCleanInstance();
        for (Subscription sub : manager.getSubscriptions(wsSession)) {
            if (sub != null && sub.isActive() && routeId.equals(sub.getRouteId())) {
                for (String p : sub.getSubscribed(null)) {
                    NavigableMap<String, Object> node = signalkModel.getSubMap(p);
                    if(logger.isDebugEnabled())logger.debug("Found node:" + p + " = " + node);
                    temp.putAll(node);
                }
            }
        }
        return temp;
    }

    /**
     * @param pathEvent the path that was changed
     */
    @Subscribe
    public void recordEvent(PathEvent pathEvent) {
        if (pathEvent == null)
            return;
        if (pathEvent.getPath() == null)
            return;

        // Send update to any non-FIXED subscribers.
        for (Subscription s : manager.getSubscriptions(wsSession)) {
            if (s.isActive() && !POLICY_FIXED.equals(s.getPolicy()) && s.isSubscribed(pathEvent.getPath())) {
                // TODO(jboynes): Batch updates together and rate limit transmissions.
                send(pathEvent.getPath());
                break;
            }
        }
    }

    @Subscribe
    public void recordEvent(DeadEvent e) {
        logger.debug("Received dead event" + e.getSource());
    }


    private void send(String path) {
        Object node = signalkModel.get(path);
        if (node == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending : " + path);
        }

        SignalKModel temp = SignalKModelFactory.getCleanInstance();
        temp.put(path, node);

        Map<String, Object> headers = new HashMap<>();
        headers.put(WebsocketConstants.CONNECTION_KEY, wsSession);
        headers.put(ConfigConstants.OUTPUT_TYPE, manager.getOutputType(wsSession));
        outProducer.sendBodyAndHeaders(temp, headers);
    }
}
