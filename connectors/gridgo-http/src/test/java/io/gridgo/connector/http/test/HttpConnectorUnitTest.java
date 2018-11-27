package io.gridgo.connector.http.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.connector.http.HttpConnector;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.impl.SimpleRegistry;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsNameResolverBuilder;

public class HttpConnectorUnitTest {

	@Test
	public void testUri() {
		String[] testCases = new String[] { //
				"http://google.com", //
				"http://google.com:80", //
				"https://google.com", //
				"https://google.com/test" //
		};
		for (String test : testCases) {
			var connector = new DefaultConnectorFactory().createConnector(test);
			Assert.assertNotNull(connector);
			Assert.assertTrue(connector instanceof HttpConnector);
		}
	}

	@Test
	public void testHttp() throws InterruptedException {
		var url = "https://raw.githubusercontent.com/dungba88/cleaner_robot/master/README.md?nameResolverBean=nameResolver";
		var eventLoopGroup = new NioEventLoopGroup();
		var factory = new DefaultConnectorFactory();
		var nameResolver = new DnsNameResolverBuilder() //
				.channelType(NioDatagramChannel.class) //
				.eventLoop(eventLoopGroup.next()) //
				.queryTimeoutMillis(1000).build();
		factory.setRegistry(new SimpleRegistry().register("nameResolver", nameResolver));

		var connector = factory.createConnector(url);
		var producer = connector.getProducer().orElseThrow();
		connector.start();

		producer.send(null);
		var latch = new CountDownLatch(2);
		var atomic = new AtomicReference<Exception>();
		producer.sendWithAck(null).always((status, response, ex) -> {
			if (ex != null)
				atomic.set(ex);
			latch.countDown();
		});
		producer.call(null).always((status, response, ex) -> {
			if (ex != null)
				atomic.set(ex);
			latch.countDown();
		});

		latch.await();

		if (atomic.get() != null)
			atomic.get().printStackTrace();

		Assert.assertNull(atomic.get());
	}
}