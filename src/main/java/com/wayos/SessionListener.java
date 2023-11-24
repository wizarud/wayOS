package com.wayos;

public interface SessionListener {
    void callback(NodeEvent nodeEvent);
    void onVariablesChanged(Session session);
    void onContextChanged(Session session, Context oldContext, Context newContext);
}
