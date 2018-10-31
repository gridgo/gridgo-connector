package io.gridgo.connector.message;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;

public interface Payload {

	BValue getId();

	BObject getHeaders();

	BElement getBody();
}
