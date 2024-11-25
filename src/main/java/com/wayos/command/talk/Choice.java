package com.wayos.command.talk;

import static java.util.Optional.ofNullable;

public class Choice {

    public final String parent;
    public final String label;
    public final String imageURL;
    public final String linkURL;

    public Choice(String parent, String label, String imageURL, String linkURL) {
        this.parent = parent;
        this.label = label;
        this.imageURL = imageURL;
        this.linkURL = linkURL;
    }

    public static Choice build(String parentId, String line) {
    	
        String [] tokens = line.split("\t");
        String label = tokens[0].trim();

        String imageURL;
        try { imageURL = tokens[1].trim(); } catch (Exception e) { imageURL = null; }

        String linkURL;
        try { linkURL = tokens[2].trim(); } catch (Exception e) { linkURL = null; }
        
        imageURL = imageURL!=null && !imageURL.isEmpty() ? imageURL : null;
        linkURL = linkURL!=null && !linkURL.isEmpty() ? linkURL : null;

        return new Choice(parentId, label, imageURL, linkURL);
    }

    public boolean isLabel() {
        return imageURL == null && linkURL == null;
    }

    public boolean isImageLabel() {
        return imageURL != null && linkURL == null;
    }

    public boolean isLinkLabel() {
        return imageURL == null && linkURL != null;
    }

    public boolean isImageLinkLabel() {
        return imageURL != null && linkURL != null;
    }

    public String toString() {

        return String.format("%s\t%s\t%s",
                ofNullable(label).orElse(""),
                ofNullable(imageURL).orElse(""),
                ofNullable(linkURL).orElse("")
                );
    }
}
