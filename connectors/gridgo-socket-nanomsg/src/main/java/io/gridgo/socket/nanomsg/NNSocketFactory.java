package io.gridgo.socket.nanomsg;

import org.nanomsg.NanoLibrary;

import io.gridgo.socket.Socket;
import io.gridgo.socket.impl.AbstractBrokerSocketFactory;
import io.gridgo.utils.helper.Assert;

public class NNSocketFactory extends AbstractBrokerSocketFactory {

	private final NanoLibrary nanomsg = new NanoLibrary();

	private Socket createNanoSocket(int type) {
		return new NNSocket(nanomsg.nn_socket(nanomsg.AF_SP, type), nanomsg);
	}

	protected Socket createCustomSocket(String type) {
		Assert.notNull(type, "Socket type");
		switch (type.toLowerCase()) {
		case "bus":
			return createNanoSocket(nanomsg.NN_BUS);
		case "surveyor":
			return createNanoSocket(nanomsg.NN_SURVEYOR);
		case "respondent":
			return createNanoSocket(nanomsg.NN_RESPONDENT);
		}
		return null;
	}

	protected Socket createPullSocket() {
		return createNanoSocket(nanomsg.NN_PULL);
	}

	protected Socket createPushSocket() {
		return createNanoSocket(nanomsg.NN_PUSH);
	}

	protected Socket createPubSocket() {
		return createNanoSocket(nanomsg.NN_PUB);
	}

	protected Socket createSubSocket() {
		return createNanoSocket(nanomsg.NN_SUB);
	}

	protected Socket createReqSocket() {
		return createNanoSocket(nanomsg.NN_REQ);
	}

	protected Socket createRepSocket() {
		return createNanoSocket(nanomsg.NN_REP);
	}

	protected Socket createPairSocket() {
		return createNanoSocket(nanomsg.NN_PAIR);
	}
}
