package com.wayos.connector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wayos.Context;
import com.wayos.ContextListener;
import com.wayos.Session;
import com.wayos.SessionListener;
import com.wayos.command.CommandNode;

@SuppressWarnings("serial")
public class SessionPool implements Serializable {

    public interface SessionPoolListener extends Serializable {
    	
        void onNewSession(RequestObject requestObject, Session session);
        void onRemoveSession(Session session);
        void onTime(Session session);
        
    }

    public interface ContextFactory extends Serializable {
    	
        Context createContext(String contextName);
        
    }
    
    public interface WakeupFactory extends Serializable {
    	
    	CommandNode createWakeup(Context context);
    	
    }
    
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * Context Pool
     */
    private Map<String, Context> contextPool = new ConcurrentHashMap<>();

    /**
     * Session Pool
     */
    private Map<String, Session> sessionPool = new ConcurrentHashMap<>();
    
    /**
     * For cleaning sessions with the given contextName
     */
    private Map<String, List<String>> contextSessionPoolIdListMap = new ConcurrentHashMap<>();
    
    private ContextListener contextListener;

    private ContextFactory contextFactory;
    
    private SessionPoolListener sessionPoolListener;
    
    private SessionListener sessionListener;

    public void register(ContextListener contextListener) {
    	
        this.contextListener = contextListener;
        
    }
    
    public void register(ContextFactory contextFactory) {
    	
        this.contextFactory = contextFactory;
        
    }
    
    public void register(SessionPoolListener sessionManagerListener) {
    	
        this.sessionPoolListener = sessionManagerListener;
        
    }
    
    public void register(SessionListener sessionListener) {
    	
    	this.sessionListener = sessionListener;
    	
    }
    
    public String generateSessionId() {
		
        lock.readLock().lock();
        
        try {
        	
        	return "" + System.nanoTime();

        } finally {
        	
            lock.readLock().unlock();
        	
        }
        
	}    
    
    public Context getContext(String contextName) {
    	
    	if (contextFactory==null) throw new RuntimeException("ContextFactory is not initialized yet!");
    	
        Context context = contextPool.get(contextName);
        
        if (context==null) {
        	
            context = contextFactory.createContext(contextName);
            
        	try {
        		
				context.load();
								
	            /**
	             * To log this context is valid or not?
	             */
	            context.validate();
	            
	            context.contextListener(contextListener);
	            
	            contextPool.put(contextName, context);
	            
			} catch (Exception e) {
				
				throw new RuntimeException(e + ":" + contextName);
			}            
                        
        }

        return context;
    }
    
    /**
     * bind session with mapSessionId (<sessionId>/<contextName>)
     */
    private String sessionPoolId(RequestObject requestObject) {
    	
    	return requestObject.sessionId() + "/" + requestObject.contextName();
    }
    
    public Session get(RequestObject requestObject) {
    	
        if (requestObject.sessionId()==null) throw new IllegalArgumentException("Null session Id:" + requestObject.sessionId());
        
        if (requestObject.contextName()==null) throw new IllegalArgumentException("Null contextName:" + requestObject.contextName());
        
        final String sessionPoolId = sessionPoolId(requestObject);
        
        Session session = null;
        
        lock.readLock().lock();
        
        try {
        	            
            final String contextName = requestObject.contextName();

            session = sessionPool.get(sessionPoolId);

            /**
             * Register sessionPoolId
             */
            if (session==null) {

            	/**
            	 * Create new session from context pool
            	 */
                session = new Session();
                
                session.register(sessionListener);
                
                session.context(getContext(contextName));
                
                /**
                 * bind context with multiple sessionIds
                 * For clear later
                 */
                List<String> sessionPoolIdList = contextSessionPoolIdListMap.get(contextName);
                
                if (sessionPoolIdList==null) {
                	
                	sessionPoolIdList = new ArrayList<>();
                	
                }
                
                sessionPoolIdList.add(sessionPoolId);
                
                contextSessionPoolIdListMap.put(contextName, sessionPoolIdList);

                sessionPool.put(sessionPoolId, session);

                /**
                 * Fire onNewSession Event!
                 */
                if (sessionPoolListener!=null) {
                	
                    sessionPoolListener.onNewSession(requestObject, session);
                }
                
            }
                        
        } finally {
        	
            lock.readLock().unlock();
            
        }

        return session;
    }

    public void clear(String contextName) {
    	
        contextPool.remove(contextName);
        
        List<String> sessionPoolIdList = contextSessionPoolIdListMap.get(contextName);
        
        if (sessionPoolIdList!=null) {
        	
            for (String sessionPoolId:sessionPoolIdList) {
            	
                sessionPool.remove(sessionPoolId);
            }
            
            clearContextIfEmptySession();
            
        }
    }
    
    public void clear(RequestObject requestObject) {
    	
        final String sessionPoolId = sessionPoolId(requestObject);
    	
        Session session = sessionPool.get(sessionPoolId);
        
        if (session==null) throw new RuntimeException("Session not found for " + sessionPoolId);
    	
        try {
        	
            if (sessionPoolListener!=null) {
            	
                sessionPoolListener.onRemoveSession(session);
            }
            
        } catch (Exception e) {
        	
        	throw new RuntimeException(e);
        	
        } finally {
        	
            sessionPool.remove(sessionPoolId);
        	
            clearContextIfEmptySession();
        	
        }

    }
        
	/**
     * Clear Context if there is no session deal with
     */
	private void clearContextIfEmptySession() {
		
        List<String> contextRemoveList = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry:contextSessionPoolIdListMap.entrySet()) {
        	
            if (entry.getValue().isEmpty()) {
            	
                contextRemoveList.add(entry.getKey());
            }
        
        }

        for (String contextName:contextRemoveList) {
        	
            contextPool.remove(contextName);
            
            contextSessionPoolIdListMap.remove(contextName);
            
        }
	}

}

