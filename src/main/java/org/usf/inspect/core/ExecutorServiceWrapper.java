package org.usf.inspect.core;

import static java.util.Objects.nonNull;
import static org.usf.inspect.core.Helper.warnNoActiveSession;
import static org.usf.inspect.core.SessionManager.currentSession;
import static org.usf.inspect.core.SessionManager.endSession;
import static org.usf.inspect.core.SessionManager.updateCurrentSession;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorServiceWrapper implements ExecutorService {
	
	@Delegate
	private final ExecutorService es;  //Future::cancel !?
	
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return aroundCallable(task, es::submit);
	}
	
	@Override
	public Future<?> submit(Runnable task) {
		return aroundRunnable(task, es::submit);
	}
	
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return aroundRunnable(task, c-> es.submit(c, result));
	}
	
	@Override
	public void execute(Runnable command) {
		aroundRunnable(command, c-> {es.execute(c); return null;});
	}
	
    private static <T> T aroundRunnable(Runnable command, Function<Runnable, T> fn) {
    	var session = currentSession();
		if(nonNull(session)) {
			session.lock(); //important! sync lock
			try {
				return fn.apply(()->{
					updateCurrentSession(session);
			    	try {
				    	command.run();
			    	}
			    	finally {
						session.unlock();
						endSession(); 
			    	}
				});
			}
			catch (Exception e) {  //@see Executor::execute
				session.unlock();
				throw e;
			}
		}
		warnNoActiveSession("");
		return fn.apply(command);
    }

    private static <T,V> V aroundCallable(Callable<T> command, Function<Callable<T>, V> fn) {
    	var session = currentSession();
		if(nonNull(session)) {
			session.lock(); //important! sync lock
			try {
				return fn.apply(()->{
					updateCurrentSession(session);
			    	try {
			    		return command.call();
			    	}
			    	finally {
						session.unlock();
						endSession(); 
			    	}
				});
			}
			catch (Exception e) {  //@see Executor::execute
				session.unlock();
				throw e;
			}
		}
		warnNoActiveSession("");
		return fn.apply(command);
    }
        
	public static ExecutorServiceWrapper wrap(@NonNull ExecutorService es) {
		return new ExecutorServiceWrapper(es);
	}
}
