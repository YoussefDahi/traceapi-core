package org.usf.traceapi.core;

import static java.lang.Thread.currentThread;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;

import java.util.List;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Helper {

	static final ThreadLocal<Session> localTrace = new InheritableThreadLocal<>();
	static final Supplier<String> idProvider = ()-> randomUUID().toString();
	
	static final DefaultUserProvider userProvider = new DefaultUserProvider(); 
	static ApplicationInfo application; //unsafe set
	
	static ApplicationInfo applicationInfo() {
		return application;
	}
	
	static DefaultUserProvider defaultUserProvider() {
		return userProvider;
	}

	static String threadName() {
		return currentThread().getName();
	}
		
	static String extractAuthScheme(List<String> authHeaders) { //nullable
		return nonNull(authHeaders) && authHeaders.size() == 1 //require one header
				? extractAuthScheme(authHeaders.get(0)) : null;
	}
	
	static String extractAuthScheme(String authHeader) { //nullable
		return nonNull(authHeader) && authHeader.matches("^\\w+ ") 
				? authHeader.substring(0, authHeader.indexOf(' ')) : null;
	}
}
