package com.wayos.context;

import java.util.List;

import com.wayos.Context;
import com.wayos.Node;

/**
 * Created by Wisarut Srisawet on 8/15/17.
 */
public class MemoryContext extends Context {

    public MemoryContext(String name) {
        super(name);
    }

    @Override
    public void doLoad(String name) throws Exception {
    	/**
    	 * TODO: why You have to rebuild json attributes?
    	 */
    	loadJSON(super.toJSONString());
    }

    @Override
    public void doSave(String name, List<Node> nodeList) {
    }

}
