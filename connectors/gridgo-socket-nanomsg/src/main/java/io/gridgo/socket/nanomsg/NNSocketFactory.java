package io.gridgo.socket.nanomsg;

import org.nanomsg.NanoLibrary;

import io.gridgo.socket.BrokerlessSocket;
import io.gridgo.socket.impl.AbstractBrokerSocketFactory;
import io.gridgo.utils.helper.Assert;

public class NNSocketFactory extends AbstractBrokerSocketFactory {

	private final NanoLibrary nanomsg = new NanoLibrary();

	private BrokerlessSocket createNanoSocket(int type) {
		return new NNSocket(nanomsg.nn_socket(nanomsg.AF_SP, type), nanomsg);
	}

	protected BrokerlessSocket createCustomSocket(String type) {
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

	protected BrokerlessSocket createPullSocket() {
		return createNanoSocket(nanomsg.NN_PULL);
	}

	protected BrokerlessSocket createPushSocket() {
		return createNanoSocket(nanomsg.NN_PUSH);
	}

	protected BrokerlessSocket createPubSocket() {
		return createNanoSocket(nanomsg.NN_PUB);
	}

	protected BrokerlessSocket createSubSocket() {
		return createNanoSocket(nanomsg.NN_SUB);
	}

	protected BrokerlessSocket createReqSocket() {
		return createNanoSocket(nanomsg.NN_REQ);
	}

	protected BrokerlessSocket createRepSocket() {
		return createNanoSocket(nanomsg.NN_REP);
	}

	protected BrokerlessSocket createPairSocket() {
		return createNanoSocket(nanomsg.NN_PAIR);
	}
}
