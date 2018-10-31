package io.gridgo.connector.message;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.utils.helper.Assert;
import lombok.Getter;

@Getter
public abstract class AbstractPayload implements Payload {

	private BValue id;
	private BObject headers;
	private BElement body;

	public void setId(Object id) {
		Assert.notNull(id, "ID");
		if (id instanceof BValue) {
			this.id = (BValue) id;
		} else {
			this.id = BValue.newDefault(id);
		}
	}

	public void setBody(BElement body) {
		Assert.notNull(body, "Body");
		this.body = body;
	}

	public void setHeaders(BObject headers) {
		Assert.notNull(headers, "Headers");
		this.headers = headers;
	}
}
