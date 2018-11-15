package io.gridgo.connector.netty4.test;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.connector.netty4.test.support.Netty4UnitTest;

public class Netty4TcpUnitTest extends Netty4UnitTest {

	@Test
	public void testTcpPingPong() throws InterruptedException, PromiseException {
		this.testPingPong("tcp", null);
	}

	@Test
	public void testHandlerException() throws InterruptedException, PromiseException {
		this.testHandlerException("tcp", null);
	}
}
