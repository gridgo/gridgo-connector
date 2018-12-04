package io.gridgo.connector.redis.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.redis.adapter.RedisConstants;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class RedisUnitTest {

	@Test
	public void testSimple() throws IOException, InterruptedException {
		var context = new DefaultConnectorContextBuilder().build();
		var connector = new DefaultConnectorFactory().createConnector("redis:single://[localhost:6379]/1", context);
		connector.start();

		try {
			assertTrue(connector.getProducer().isPresent());
			Producer producer = connector.getProducer().get();

			BObject object = BObject.newDefault();
			object.setAny("key", "5").setAny("value", "do thanh tung");
			BElement bElement = BElement.fromAny(object);

			var headers = BObject.newDefault().setAny(RedisConstants.COMMAND, RedisConstants.COMMAND_APPEND);
			producer.call(Message.newDefault(Payload.newDefault(headers, bElement))).done(response -> {
				System.out.println(response.getPayload().getBody().asValue().getLong());
			}).fail(ex -> {
				System.out.println("Error while handling request");
			});

			// TODO add more aserts here...
		} finally {
			connector.stop();
		}
	}

}
