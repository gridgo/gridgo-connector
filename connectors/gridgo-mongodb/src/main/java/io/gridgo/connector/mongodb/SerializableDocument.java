package io.gridgo.connector.mongodb;

import org.bson.Document;

import io.gridgo.bean.SerializableReference;
import lombok.NonNull;

public class SerializableDocument implements SerializableReference {

	private final Document doc;

	public SerializableDocument(final @NonNull Document doc) {
		this.doc = doc;
	}

	@Override
	public String toXml() {
		throw new UnsupportedOperationException("BSON cannot be converted to XML");
	}

	@Override
	public String toJson() {
		return doc.toJson();
	}
}
