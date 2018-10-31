package io.gridgo.socket.netty4;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.impl.AbstractSocket;
import io.netty.buffer.ByteBuf;

public class Netty4Socket extends AbstractSocket<ByteBuf> {

	
	
	@Override
	public int send(ByteBuf message, boolean block) {
		return 0;
	}

	@Override
	public void applyConfig(String name, Object value) {

	}

	@Override
	protected void doClose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doConnect(Endpoint endpoint) {
		
	}

	@Override
	protected void doBind(Endpoint endpoint) {
		// TODO Auto-generated method stub

	}

}
