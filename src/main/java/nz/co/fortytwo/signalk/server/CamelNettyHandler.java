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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.fortytwo.signalk.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mjson.Json;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


@Sharable
public class CamelNettyHandler extends SimpleChannelInboundHandler<String> {

	private Logger logger = Logger.getLogger(CamelNettyHandler.class);
	private BiMap<String,ChannelHandlerContext> contextList = HashBiMap.create();
	private String outputType;
	//@Produce(uri = RouteManager.SEDA_INPUT)
    ProducerTemplate producer;
    
    private final AttributeKey<Map<String, Object>> msgHeaders =
            AttributeKey.valueOf("msgHeaders");
	
	public CamelNettyHandler( String outputType) throws Exception {
		this.outputType=outputType;
		producer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		producer.setDefaultEndpointUri(RouteManager.SEDA_INPUT );
		producer.start();
		
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Send greeting for a new connection.
		ctx.write(Util.getWelcomeMsg().toString() + "\r\n");
		ctx.flush();
		String session = UUID.randomUUID().toString();
		String localAddress = ctx.channel().localAddress().toString();
		String remoteAddress = ctx.channel().remoteAddress().toString();
		SubscriptionManagerFactory.getInstance().add(session, session, outputType,localAddress, remoteAddress);
		contextList.put(session, ctx);
		//make up headers
		ctx.attr(msgHeaders).set(getHeaders(ctx));
		
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// unsubscribe all
		String session = contextList.inverse().get(ctx);
		SubscriptionManagerFactory.getInstance().removeSessionId(session);
		super.channelInactive(ctx);
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
		if(logger.isDebugEnabled())logger.debug("Request:" + request);
		
		producer.sendBodyAndHeaders(request, ctx.attr(msgHeaders).get());
	}

	private Map<String, Object> getHeaders(ChannelHandlerContext ctx) throws Exception {
		Map<String, Object> headers = new HashMap<>();
		headers.put(WebsocketConstants.CONNECTION_KEY, contextList.inverse().get(ctx));
		String remoteAddress = ctx.channel().remoteAddress().toString();
		headers.put(SignalKConstants.MSG_SRC_IP, remoteAddress);
		headers.put(ConfigConstants.OUTPUT_TYPE, outputType);
		String localAddress = ctx.channel().localAddress().toString();
		
		if(Util.sameNetwork(localAddress, remoteAddress)){
			headers.put(SignalKConstants.MSG_TYPE, SignalKConstants.INTERNAL_IP);
		}else{
			headers.put(SignalKConstants.MSG_TYPE, SignalKConstants.EXTERNAL_IP);
		}
		return headers;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public ChannelHandlerContext getChannel(String sessionId){
		return contextList.get(sessionId);
	}
	
	public Map<String,ChannelHandlerContext> getContextList() {
		return contextList;
	}

	@Override
	public boolean acceptInboundMessage(Object msg) throws Exception {
		if(msg instanceof Json || msg instanceof String)return true;
		return super.acceptInboundMessage(msg);
	}

	

}
