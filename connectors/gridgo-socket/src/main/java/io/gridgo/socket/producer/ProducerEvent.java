package io.gridgo.socket.producer;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BArray;
import io.gridgo.framework.support.Message;
import lombok.Data;

@Data
final class ProducerEvent {

	private final BArray data = BArray.newDefault();

	private Deferred<Message, Exception> deferred;

	void clear() {
		this.data.clear();
		this.deferred = null;
	}

}
