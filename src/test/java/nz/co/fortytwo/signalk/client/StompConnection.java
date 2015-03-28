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
package nz.co.fortytwo.signalk.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.activemq.transport.stomp.StompFrame;
import org.apache.activemq.transport.stomp.StompWireFormat;

public class StompConnection {
	public static final long RECEIVE_TIMEOUT = 10000L;
	private Socket stompSocket;
	private ByteArrayOutputStream inputBuffer;
	private String version;

	public StompConnection() {
		this.inputBuffer = new ByteArrayOutputStream();
		this.version = "1.0";
	}

	public void open(String host, int port) throws IOException, UnknownHostException {
		open(new Socket(host, port));
	}

	public void open(Socket socket) {
		this.stompSocket = socket;
	}

	public void close() throws IOException {
		if (this.stompSocket != null) {
			this.stompSocket.close();
			this.stompSocket = null;
		}
	}

	public void sendFrame(String data) throws Exception {
		byte[] bytes = data.getBytes("UTF-8");
		OutputStream outputStream = this.stompSocket.getOutputStream();
		outputStream.write(bytes);
		outputStream.flush();
	}

	public void sendFrame(String frame, byte[] data) throws Exception {
		byte[] bytes = frame.getBytes("UTF-8");
		OutputStream outputStream = this.stompSocket.getOutputStream();
		outputStream.write(bytes);
		outputStream.write(data);
		outputStream.flush();
	}

	public StompFrame receive() throws Exception {
		return receive(10000L);
	}

	public StompFrame receive(long timeOut) throws Exception {
		this.stompSocket.setSoTimeout((int) timeOut);
		InputStream is = this.stompSocket.getInputStream();
		StompWireFormat wf = new StompWireFormat();
		wf.setStompVersion(this.version);
		DataInputStream dis = new DataInputStream(is);
		return ((StompFrame) wf.unmarshal(dis));
	}

	public String receiveFrame() throws Exception {
		return receiveFrame(10000L);
	}

	public String receiveFrame(long timeOut) throws Exception {
		this.stompSocket.setSoTimeout((int) timeOut);
		InputStream is = this.stompSocket.getInputStream();
		int c = 0;
		while (true) {
			c = is.read();
			if (c < 0)
				throw new IOException("socket closed.");
			if (c == 0) {
				c = is.read();
				if (c == 10) {
					return stringFromBuffer(this.inputBuffer);
				}
				this.inputBuffer.write(0);
				this.inputBuffer.write(c);
			}

			this.inputBuffer.write(c);
		}
	}

	private String stringFromBuffer(ByteArrayOutputStream inputBuffer) throws Exception {
		byte[] ba = inputBuffer.toByteArray();
		inputBuffer.reset();
		return new String(ba, "UTF-8");
	}

	public Socket getStompSocket() {
		return this.stompSocket;
	}

	public void setStompSocket(Socket stompSocket) {
		this.stompSocket = stompSocket;
	}

	public StompFrame connect(String username, String password) throws Exception {
		return connect(username, password, null);
	}

	public StompFrame connect(String username, String password, String client) throws Exception {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("login", username);
		headers.put("passcode", password);
		if (client != null) {
			headers.put("client-id", client);
		}
		return connect(headers);
	}

	public StompFrame connect(HashMap<String, String> headers) throws Exception {
		StompFrame frame = new StompFrame("CONNECT", headers);
		sendFrame(frame.format());

		StompFrame connect = receive();
		if (!(connect.getAction().equals("CONNECTED")))
			throw new Exception(new StringBuilder().append("Not connected: ").append(connect.getBody()).toString());
		return connect;
	}

	public void disconnect() throws Exception {
		disconnect(null);
	}

	public void disconnect(String receiptId) throws Exception {
		StompFrame frame = new StompFrame("DISCONNECT");
		if ((receiptId != null) && (!(receiptId.isEmpty()))) {
			frame.getHeaders().put("receipt", receiptId);
		}
		sendFrame(frame.format());
	}

	public void send(String destination, String message) throws Exception {
		send(destination, message, null, null);
	}

	public void send(String destination, String message, String transaction, HashMap<String, String> headers) throws Exception {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put("destination", destination);
		if (transaction != null) {
			headers.put("transaction", transaction);
		}
		StompFrame frame = new StompFrame("SEND", headers, message.getBytes());
		sendFrame(frame.format());
	}

	public void subscribe(String destination) throws Exception {
		subscribe(destination, null, null);
	}

	public void subscribe(String destination, String ack) throws Exception {
		subscribe(destination, ack, new HashMap<String, String>());
	}

	public void subscribe(String destination, String ack, HashMap<String, String> headers) throws Exception {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put("destination", destination);
		if (ack != null) {
			headers.put("ack", ack);
		}
		StompFrame frame = new StompFrame("SUBSCRIBE", headers);
		sendFrame(frame.format());
	}

	public void unsubscribe(String destination) throws Exception {
		unsubscribe(destination, null);
	}

	public void unsubscribe(String destination, HashMap<String, String> headers) throws Exception {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put("destination", destination);
		StompFrame frame = new StompFrame("UNSUBSCRIBE", headers);
		sendFrame(frame.format());
	}

	public void begin(String transaction) throws Exception {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("transaction", transaction);
		StompFrame frame = new StompFrame("BEGIN", headers);
		sendFrame(frame.format());
	}

	public void abort(String transaction) throws Exception {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("transaction", transaction);
		StompFrame frame = new StompFrame("ABORT", headers);
		sendFrame(frame.format());
	}

	public void commit(String transaction) throws Exception {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("transaction", transaction);
		StompFrame frame = new StompFrame("COMMIT", headers);
		sendFrame(frame.format());
	}

	public void ack(StompFrame frame) throws Exception {
		ack((String) frame.getHeaders().get("message-id"), null);
	}

	public void ack(StompFrame frame, String transaction) throws Exception {
		ack((String) frame.getHeaders().get("message-id"), transaction);
	}

	public void ack(String messageId) throws Exception {
		ack(messageId, null);
	}

	public void ack(String messageId, String transaction) throws Exception {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("message-id", messageId);
		if (transaction != null)
			headers.put("transaction", transaction);
		StompFrame frame = new StompFrame("ACK", headers);
		sendFrame(frame.format());
	}

	public void keepAlive() throws Exception {
		OutputStream outputStream = this.stompSocket.getOutputStream();
		outputStream.write(10);
		outputStream.flush();
	}

	protected String appendHeaders(HashMap<String, Object> headers) {
		StringBuilder result = new StringBuilder();
		for (String key : headers.keySet()) {
			result.append(new StringBuilder().append(key).append(":").append(headers.get(key)).append("\n").toString());
		}
		result.append("\n");
		return result.toString();
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
