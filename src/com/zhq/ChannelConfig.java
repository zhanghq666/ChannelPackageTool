package com.zhq;

public class ChannelConfig {
    private String nameIdentify;
    private String value;

    public String getNameIdentify() {
        return nameIdentify;
    }

    public void setNameIdentify(String nameIdentify) {
        this.nameIdentify = nameIdentify;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ChannelConfig{" +
                "nameIdentify='" + nameIdentify + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
