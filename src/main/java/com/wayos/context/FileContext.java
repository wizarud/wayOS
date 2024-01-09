package com.wayos.context;

import com.wayos.Context;
import com.wayos.Node;

import x.org.json.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by eoss-th on 8/15/17.
 */
public class FileContext extends Context {

    private File dir;

    public FileContext(File dir, String name) {
        super(name);
        this.dir = dir;
    }

    public FileContext(String name) {
        this(null, name);
    }

    public File getDir() {
        if (dir.isDirectory())
            return dir;
        return null;
    }

    private File getFile(String name) {
        File file;
        if (dir!=null&&dir.isDirectory()) {
            file = new File(dir, name + SUFFIX);
        } else {
            file = new File(name + SUFFIX);
        }
        return file;
    }

    @Override
    public void doLoad(String name) throws Exception {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(getFile(name)), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine())!=null) {
                sb.append(line);
            }

            loadJSON(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (br!=null) try { br.close(); } catch (Exception e) {}
        }
    }

    @Override
    public void doSave(String name, List<Node> nodeList) {
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(
                    new FileOutputStream(getFile(name), false), StandardCharsets.UTF_8);
            out.write(toJSONString());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out!=null) try {out.close();} catch (Exception e) {}
        }
    }
    
    public static boolean save(String toFileName, Context context, boolean overwrite) {
    	
    	if (!overwrite && new File(toFileName).exists()) return false;
    	
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(
                    new FileOutputStream(toFileName + SUFFIX, false), StandardCharsets.UTF_8);
            out.write(context.toJSONString());
            out.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out!=null) try {out.close();} catch (Exception e) {}
        }
    	return false;
    }

}
