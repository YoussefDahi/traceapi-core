package org.usf.traceapi.core;

import static java.util.Objects.nonNull;
import static org.usf.traceapi.core.Helper.localTrace;
import static org.usf.traceapi.core.Helper.newInstance;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * @author u$f
 *
 */
public final class IncomingRequestInterceptor implements HandlerInterceptor { //AsyncHandlerInterceptor ?

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return handler instanceof HandlerMethod; //important! !static resource 
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) throws Exception {
        HandlerMethod m = (HandlerMethod) handler;
        TraceableApi a = m.getMethodAnnotation(TraceableApi.class);
        if(nonNull(a)) {
        	var trace = (IncomingRequest) localTrace.get();
            if(nonNull(trace)) {
            	if(a.clientProvider() != DefaultUserProvider.class) {
            		trace.setUser(newInstance(a.clientProvider())
            				.map(p-> p.getUser(req))
            				.orElse(null));
            	}
            	if(!a.value().isEmpty()) {
            		trace.setName(a.value());
            	}
            }
        }
    }
    
}
