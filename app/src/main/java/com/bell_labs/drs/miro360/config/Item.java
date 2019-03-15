package com.bell_labs.drs.miro360.config;

public class Item {
    public String scale = Sequence.NONE;
    public String text = Sequence.NONE;
    public String tag = "ITEM";

    public Item() {}

    public Item(String scale, String text, String tag) {
        this.scale = scale;
        this.text = text;
        this.tag = tag;
    }
}
