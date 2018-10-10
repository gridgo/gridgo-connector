package io.gridgo.socket.helper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Endpoint {

	private String address;

	private String protocol;

	private String host;

	@Builder.Default
	private int port = -1;

	public String getResolvedAddress() {
		return this.getProtocol() + "://" + host + (port <= 0 ? "" : (":" + port));
	}

	@Override
	public String toString() {
		return this.getAddress();
	}
}
