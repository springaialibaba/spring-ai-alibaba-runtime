package com.alibaba.cloud.ai.a2a.server;

import io.a2a.server.auth.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCallContext {

	private final Map<Object, Object> modelConfig = new ConcurrentHashMap();

	private final Map<String, Object> state;

	private final User user;

	public ServerCallContext(User user, Map<String, Object> state) {
		this.user = user;
		this.state = state;
	}

	public Map<String, Object> getState() {
		return this.state;
	}

	public User getUser() {
		return this.user;
	}

}
