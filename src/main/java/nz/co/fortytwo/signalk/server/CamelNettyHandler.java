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

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import mjson.Json;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


@Sharable
public class CamelNettyHandler extends SimpleChannelInboundHandler<String> {

	private Logger logger = Logger.getLogger(CamelNettyHandler.class.getSimpleName());
	private BiMap<String,ChannelHandlerContext> contextList = HashBiMap.create();

	//@Produce(uri = RouteManager.SEDA_INPUT)
    ProducerTemplate producer;
	
	public CamelNettyHandler(Properties config) throws Exception {
		producer= new DefaultProducerTemplate(CamelContextFactory.getInstance());
		producer.setDefaultEndpointUri(RouteManager.SEDA_INPUT );
		producer.start();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Send greeting for a new connection.
		ctx.write("Welcome to signalk at " + InetAddress.getLocalHost().getHostName() + "!\r\n");
		ctx.write("It is " + new Date() + " now.\r\n");
		ctx.flush();
		String session = UUID.randomUUID().toString();
		SubscriptionManagerFactory.getInstance().add(session, session);
		contextList.put(session, ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
		logger.debug("Request:" + request);
		Map headers = getHeaders(ctx);
		producer.sendBodyAndHeaders(request, headers);
	}

	private Map getHeaders(ChannelHandlerContext ctx) {
		Map<String, String> headers = new HashMap<>();
		headers.put(WebsocketConstants.CONNECTION_KEY, contextList.inverse().get(ctx));
		headers.put(RouteManager.REMOTE_ADDRESS, ctx.channel().remoteAddress().toString());
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
