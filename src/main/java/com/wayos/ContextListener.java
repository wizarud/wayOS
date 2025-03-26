package com.wayos;

import java.io.Serializable;

/**
 * Created by Wisarut Srisawet on 9/19/2017 AD.
 */
public interface ContextListener extends Serializable {
    void callback(NodeEvent nodeEvent);
}
