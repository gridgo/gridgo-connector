package io.gridgo.connector.test.support;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConsumer;

public class TestConsumer extends AbstractConsumer {

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

	}

	public void testPublish() {
		publish(createMessage(BObject.newDefault().setAny("test-header", 1), null), null);
	}

}
