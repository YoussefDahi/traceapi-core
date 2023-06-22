package org.usf.traceapi.core;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.time.Instant.ofEpochMilli;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.traceapi.core.IncomingRequestFilter.TRACE_HEADER;
import static org.usf.traceapi.core.TraceConfiguration.idProvider;
import static org.usf.traceapi.core.TraceConfiguration.localTrace;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public final class OutcomingRequestInterceptor implements ClientHttpRequestInterceptor {
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		var trc = localTrace.get();
		if(isNull(trc)) {
			return execution.execute(request, body);
		}
		//else main request
		ClientHttpResponse res = null;
		var out = new OutcomingRequest(idProvider.get());
		request.getHeaders().add(TRACE_HEADER, out.getId());
		var beg = currentTimeMillis();
		try {
			res = execution.execute(request, body);
		}
		finally {
			var fin = currentTimeMillis();
			try {
				out.setMethod(request.getMethodValue());
				out.setProtocol(request.getURI().getScheme());
				out.setHost(request.getURI().getHost());
				out.setPort(request.getURI().getPort());
				out.setPath(request.getURI().getPath());
				out.setQuery(request.getURI().getQuery());
				out.setStart(ofEpochMilli(beg));
				out.setEnd(ofEpochMilli(fin));
				out.setInDataSize(nonNull(body) ? body.length : 0); //not exact !?
				out.setThread(currentThread().getName());
				if(nonNull(res)) {
					out.setStatus(res.getRawStatusCode());
					out.setOutDataSize(res.getBody().available()); //not exact !?
				}
				trc.append(out);
			}
			catch(Exception e) {
				log.warn("error while tracing : {}" + request, e);
				//do not catch exception
			}
		}
		return res;
	}
	
}