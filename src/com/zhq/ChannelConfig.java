package com.zhq;

public class ChannelConfig {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ChannelConfig{" +
                "value='" + value + '\'' +
                '}';
    }
}
