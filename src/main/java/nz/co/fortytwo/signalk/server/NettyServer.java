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

/*
 * Adapted from https://github.com/rhq-project/rhq-metrics
 */

package nz.co.fortytwo.signalk.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;

import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

public class NettyServer implements Processor{

	
	private final EventLoopGroup group;
	private final EventLoopGroup workerGroup;

	private static Logger logger = LogManager.getLogger(NettyServer.class);
	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();
	private CamelNettyHandler forwardingHandler = null;
	private CamelUdpNettyHandler udpHandler = null;
	private Channel udpChannel = null;
	private int tcpPort = Util.getConfigPropertyInt(ConfigConstants.TCP_PORT);
	private int udpPort = Util.getConfigPropertyInt(ConfigConstants.UDP_PORT);
	private String outputType;
	/**
	 * @param configDir
	 * @throws Exception 
	 */
	public NettyServer(String configDir,String outputType) throws Exception {
		
		group = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		this.outputType=outputType;
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				shutdownServer();
			}
		}));
	}
	
	public void run() throws Exception{
		forwardingHandler = new CamelNettyHandler(outputType);
		// The generic TCP socket server
		ServerBootstrap skBootstrap = new ServerBootstrap();
		skBootstrap.group(group, workerGroup).channel(NioServerSocketChannel.class).localAddress(tcpPort)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel socketChannel) throws Exception {
						ChannelPipeline pipeline = socketChannel.pipeline();
						pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
						pipeline.addLast(DECODER);
						pipeline.addLast(ENCODER);
						pipeline.addLast(forwardingHandler);
						logger.info("Signal K "+outputType+" Connection over TCP from:" + socketChannel.remoteAddress());
						
					}
					
				});
		final ChannelFuture signalkTcpFuture = skBootstrap.bind().sync();
		logger.info("Server listening on TCP " + signalkTcpFuture.channel().localAddress());
		signalkTcpFuture.channel().closeFuture();
		
		if(udpPort>0){
			udpHandler = new CamelUdpNettyHandler(outputType);
			 
			Bootstrap udpBootstrap = new Bootstrap();
			udpBootstrap.group(group).channel(NioDatagramChannel.class)
				.option(ChannelOption.SO_BROADCAST, true)
				.handler(udpHandler);
			udpChannel = udpBootstrap.bind(tcpPort-1).sync().channel();
			logger.info("Server listening on UDP " + udpChannel.localAddress());
		}
	}

	public void shutdownServer() {
		logger.info("Stopping ptrans...");
		Future<?> groupShutdownFuture = group.shutdownGracefully();
		Future<?> workerGroupShutdownFuture = workerGroup.shutdownGracefully();
		try {
			groupShutdownFuture.sync();
		} catch (InterruptedException ignored) {
		}
		try {
			workerGroupShutdownFuture.sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("Stopped");
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.debug("Received msg : "+exchange.getIn().getBody());
		String msg = exchange.getIn().getBody().toString();
		if(msg!=null){
			//get the session
			String session = exchange.getIn().getHeader(WebsocketConstants.CONNECTION_KEY, String.class);
			
			if(WebsocketConstants.SEND_TO_ALL.equals(session)){
				//udp
				if(udpPort>0 && udpChannel!=null&& udpChannel.isWritable()){
					for(InetSocketAddress client:udpHandler.getSessionList().values()){
						if(logger.isDebugEnabled())logger.debug("Sending udp: "+exchange.getIn().getBody());
						//udpCtx.pipeline().writeAndFlush(msg+"\r\n");
						udpChannel.writeAndFlush(new DatagramPacket(
							Unpooled.copiedBuffer(msg+"\r\n", CharsetUtil.UTF_8),client));
						if(logger.isDebugEnabled())logger.debug("Sent udp to "+client);
					}
				}
				//tcp
				for(String key: forwardingHandler.getContextList().keySet()){
					ChannelHandlerContext ctx = forwardingHandler.getChannel(key);
					if(ctx!=null&& ctx.channel().isWritable())ctx.pipeline().writeAndFlush(msg+"\r\n");
				}
			}else{
				//udp
				if(udpPort>0 && udpChannel!=null&& udpChannel.isWritable()){
					final InetSocketAddress client = udpHandler.getSessionList().get(session);
					if(logger.isDebugEnabled())logger.debug("Sending udp: "+exchange.getIn().getBody());
					//udpCtx.pipeline().writeAndFlush(msg+"\r\n");
					udpChannel.writeAndFlush(new DatagramPacket(
						Unpooled.copiedBuffer(msg+"\r\n", CharsetUtil.UTF_8),client));
					if(logger.isDebugEnabled())logger.debug("Sent udp for session: "+session);
					//TODO: how do we tell when a UDP client is gone
				}
				//tcp
				ChannelHandlerContext ctx = forwardingHandler.getChannel(session);
				if(ctx!=null && ctx.channel().isWritable())ctx.pipeline().writeAndFlush(msg+"\r\n");
			}
		}
		
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int port) {
		this.tcpPort = port;
	}

	protected int getUdpPort() {
		return udpPort;
	}

	protected void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}

}
