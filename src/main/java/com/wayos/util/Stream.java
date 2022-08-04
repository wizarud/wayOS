package com.wayos.util;

import java.io.InputStream;

/**
 * Created by eossth on 9/21/2017 AD.
 */

public abstract class Stream {
    public abstract String read();
    public abstract void write(String text);
    public abstract void write(InputStream inputStream) throws Exception;
}
