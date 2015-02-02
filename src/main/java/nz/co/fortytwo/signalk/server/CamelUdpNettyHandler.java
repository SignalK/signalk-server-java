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
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ConcurrentSet;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Set;

import mjson.Json;

import org.apache.log4j.Logger;


@Sharable
public class CamelUdpNettyHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private Logger logger = Logger.getLogger(CamelUdpNettyHandler.class.getSimpleName());
	
	private Set<InetSocketAddress> clients = new ConcurrentSet<InetSocketAddress>();

	
	
	public CamelUdpNettyHandler(Properties config) throws Exception {
		
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Send greeting for a new connection.
		NioDatagramChannel udpChannel = (NioDatagramChannel) ctx.channel();
		
		if(logger.isDebugEnabled())logger.debug("channelActive:" + udpChannel.remoteAddress());
		
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
		String request = packet.content().toString(CharsetUtil.UTF_8);
		if(logger.isDebugEnabled())logger.debug("Sender "+packet.sender()+" sent request:" + request);
		clients.add(packet.sender());
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

	@Override
	public boolean acceptInboundMessage(Object msg) throws Exception {
		if(msg instanceof Json || msg instanceof String)return true;
		return super.acceptInboundMessage(msg);
	}

	protected Set<InetSocketAddress> getClients() {
		return clients;
	}

	
}
