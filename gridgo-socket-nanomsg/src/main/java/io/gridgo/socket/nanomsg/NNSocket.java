package io.gridgo.socket.nanomsg;

import java.nio.ByteBuffer;

import org.nanomsg.NanoLibrary;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.impl.AbstractSocket;
import io.gridgo.utils.PrimitiveUtils;
import io.gridgo.utils.helper.Assert;
import lombok.AccessLevel;
import lombok.Getter;

public class NNSocket extends AbstractSocket {

	@Getter(AccessLevel.PROTECTED)
	private final int id;

	@Getter(AccessLevel.PROTECTED)
	private final NanoLibrary nanomsg;

	NNSocket(int id, NanoLibrary nanoLibrary) {
		this.id = id;
		this.nanomsg = Assert.notNull(nanoLibrary, "Nanomsg");
	}

	@Override
	protected void doClose() {
		nanomsg.nn_close(getId());
	}

	@Override
	protected void doConnect(Endpoint endpoint) {
		nanomsg.nn_connect(getId(), endpoint.getResolvedAddress());
	}

	@Override
	protected void doBind(Endpoint endpoint) {
		nanomsg.nn_bind(getId(), endpoint.getResolvedAddress());
	}

	@Override
	protected int doSend(ByteBuffer buffer, boolean block) {
		return nanomsg.nn_send(getId(), buffer, block ? 0 : nanomsg.NN_DONTWAIT);
	}

	@Override
	protected int doReveive(ByteBuffer buffer, boolean block) {
		return nanomsg.nn_recv(id, buffer, block ? 0 : nanomsg.NN_DONTWAIT);
	}

	private void applyConfig(int option, int value) {
		boolean success = nanomsg.nn_setsockopt_int(this.getId(), nanomsg.NN_SOL_SOCKET, option, value) >= 0;
		if (!success) {
			throw new NNException("Cannot apply option " + option + " to nnsocket id " + this.getId());
		}
	}

	@Override
	public void applyConfig(String name, Object value) {
		Assert.notNull(name, "Config's name");
		Assert.notNull(value, "Config's value");

		switch (name.toLowerCase()) {
		case "linger":
			this.applyConfig(nanomsg.NN_LINGER, PrimitiveUtils.getIntegerValueFrom(value));
			break;
		case "sendbuffer":
		case "sndbuf":
			this.applyConfig(nanomsg.NN_SNDBUF, PrimitiveUtils.getIntegerValueFrom(value));
			break;
		case "receivebuffer":
		case "recvbuffer":
		case "recvbuf":
		case "rcvbuf":
			this.applyConfig(nanomsg.NN_RCVBUF, PrimitiveUtils.getIntegerValueFrom(value));
			break;
		case "sendtimeout":
		case "sndtimeout":
		case "sendtimeo":
		case "sndtimeo":
			this.applyConfig(nanomsg.NN_SNDTIMEO, PrimitiveUtils.getIntegerValueFrom(value));
			break;
		case "receivetimeout":
		case "recvtimeout":
		case "rcvtimeo":
			this.applyConfig(nanomsg.NN_RCVTIMEO, PrimitiveUtils.getIntegerValueFrom(value));
			break;
		case "reconnectinterval":
		case "reconnect_ivl":
		case "reconnectivl":
			this.applyConfig(nanomsg.NN_RECONNECT_IVL, PrimitiveUtils.getIntegerValueFrom(value));
			break;
		case "reconnectivlmax":
		case "reconnect_ivl_max":
		case "reconnectintervalmax":
			this.applyConfig(nanomsg.NN_RECONNECT_IVL_MAX, PrimitiveUtils.getIntegerValueFrom(value));
			break;
		case "sendpriority":
		case "sndpriority":
		case "sendprio":
		case "sndprio":
			this.applyConfig(nanomsg.NN_SNDPRIO, PrimitiveUtils.getIntegerValueFrom(value));
			break;
		}
	}
}
