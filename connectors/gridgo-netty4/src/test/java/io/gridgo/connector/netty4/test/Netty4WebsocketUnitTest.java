package io.gridgo.connector.netty4.test;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.connector.netty4.test.support.Netty4UnitTest;

public class Netty4WebsocketUnitTest extends Netty4UnitTest {

	@Test
	public void testWsPingPong() throws InterruptedException, PromiseException {
		this.testPingPong("ws", "test");
	}

	@Test
	public void testHandlerException() throws InterruptedException, PromiseException {
		this.testHandlerException("ws", "test");
	}
}
