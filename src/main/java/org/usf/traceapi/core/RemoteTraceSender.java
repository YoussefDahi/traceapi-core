package org.usf.traceapi.core;

import static java.util.Collections.emptyList;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.stream.Collectors.toCollection;
import static org.usf.traceapi.core.Helper.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author u$f
 *
 */
public final class RemoteTraceSender implements TraceHandler {
	
	static final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();
    private final BlockingQueue<Session> queue = new LinkedBlockingQueue<>();

	private final TraceConfigurationProperties properties;
	private final RestTemplate template;

	public RemoteTraceSender(TraceConfigurationProperties properties) {
		this(properties, new RestTemplate());
	}
	
	public RemoteTraceSender(TraceConfigurationProperties properties, RestTemplate template) {
		this.properties = properties;
		this.template = template;
    	executor.scheduleWithFixedDelay(this::sendAll, properties.getDelay(), properties.getDelay(), properties.getUnit());
	}
	
	@Override
	public void handle(Session session) {
		queue.add(session);
		log.debug("new session added to the queue : {} session(s)", queue.size());
	}
	
    private void sendAll() {
        var list = completedSession();
        if(!list.isEmpty()) {
	        log.info("scheduled data queue sending.. : {} session(s)", list.size());
	        try {
	        	template.put(properties.getUrl(), list);
	    	}
	    	catch (Exception e) {
	    		queue.addAll(list); // retry later
	    		log.error("error while sending sessions", e);
			}
        }
    }
    
	private List<Session> completedSession() {
		List<Session> sub = queue.isEmpty() 
    			? emptyList() 
    			: queue.stream().filter(Session::wasCompleted).collect(toCollection(SessionList::new));
    	if(!sub.isEmpty()) {
    		queue.removeAll(sub);
    	}
    	return sub;
	}
	
	@SuppressWarnings("serial") //Jackson issue https://github.com/FasterXML/jackson-databind/issues/23
	static class SessionList extends ArrayList<Session> {}
}
