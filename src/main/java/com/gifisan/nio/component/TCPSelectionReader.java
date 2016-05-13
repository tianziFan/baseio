package com.gifisan.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.protocol.tcp.ProtocolDecoder;
import com.gifisan.nio.server.NIOContext;

public class TCPSelectionReader implements SelectionAcceptor {

	private ReadFutureAcceptor	readFutureAcceptor	= null;
	private NIOContext			context			= null;
	private EndPointWriter		endPointWriter		= null;

	public TCPSelectionReader(NIOContext context,EndPointWriter endPointWriter) {
		this.context = context;
		this.endPointWriter = endPointWriter;
		this.readFutureAcceptor = context.getReadFutureAcceptor();
	}
	
	private TCPEndPoint getEndPoint(SelectionKey selectionKey) throws SocketException{
		
		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();
		
		if (endPoint == null) {
			endPoint = new DefaultTCPEndPoint(context, selectionKey,endPointWriter);
			selectionKey.attach(endPoint);
		}
		return endPoint;
		
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;

		TCPEndPoint endPoint = getEndPoint(selectionKey);

		if (endPoint.isEndConnect()) {
			return;
		}

		IOReadFuture future = endPoint.getReadFuture();

		if (future == null) {

			ProtocolDecoder decoder = context.getProtocolDecoder();

			future = decoder.decode(endPoint);

			if (future == null) {
				if (endPoint.isEndConnect()) {
					CloseUtil.close(endPoint);
				}
				return;
			}

			endPoint.setReadFuture(future);
		}

		if (future.read()) {

			endPoint.setReadFuture(null);

			readFutureAcceptor.accept(future.getSession(), future);
		}
	}

}