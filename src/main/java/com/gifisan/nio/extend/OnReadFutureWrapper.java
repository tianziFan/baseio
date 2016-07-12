package com.gifisan.nio.extend;

import com.gifisan.nio.component.concurrent.LinkedList;
import com.gifisan.nio.component.concurrent.LinkedListM2O;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public class OnReadFutureWrapper implements OnReadFuture{
	
	private OnReadFuture listener = null;
	
	private LinkedList<WaiterOnReadFuture> waiters = new LinkedListM2O<WaiterOnReadFuture>();
	
	public void onResponse(final FixedSession session, final NIOReadFuture future) {
		
		WaiterOnReadFuture waiter = waiters.poll();
		
		if (waiter != null) {
			
			waiter.onResponse(session, future);
			
			return;
		}
		
		if (listener == null) {
			return;
		}
		
		listener.onResponse(session, future);
	}
	
	public void listen(WaiterOnReadFuture onReadFuture){
		this.waiters.offer(onReadFuture);
	}

	public OnReadFuture getListener() {
		return listener;
	}

	public void setListener(OnReadFuture listener) {
		this.listener = listener;
	}
}