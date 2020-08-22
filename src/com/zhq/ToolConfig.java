package com.zhq;

/**
 * 工具的配置实体类
 */
public class ToolConfig {
    private String tencentSid;
    private String tencentSkey;

    private String keystoreFilePath;
    private String keyPassword;
    private String storePassword;
    private String keyAlias;

    private String targetApkPath;

    private String channelMask;
    private String channelConfigPath;

    private boolean showDetailLog;


    public String getTencentSid() {
        return tencentSid;
    }

    public void setTencentSid(String tencentSid) {
        this.tencentSid = tencentSid;
    }

    public String getTencentSkey() {
        return tencentSkey;
    }

    public void setTencentSkey(String tencentSkey) {
        this.tencentSkey = tencentSkey;
    }

    public String getKeystoreFilePath() {
        return keystoreFilePath;
    }

    public void setKeystoreFilePath(String keystoreFilePath) {
        this.keystoreFilePath = keystoreFilePath;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getStorePassword() {
        return storePassword;
    }

    public void setStorePassword(String storePassword) {
        this.storePassword = storePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getTargetApkPath() {
        return targetApkPath;
    }

    public void setTargetApkPath(String targetApkPath) {
        this.targetApkPath = targetApkPath;
    }

    public String getChannelConfigPath() {
        return channelConfigPath;
    }

    public void setChannelConfigPath(String channelConfigPath) {
        this.channelConfigPath = channelConfigPath;
    }

    public String getChannelMask() {
        return channelMask;
    }

    public void setChannelMask(String channelMask) {
        this.channelMask = channelMask;
    }

    public boolean isShowDetailLog() {
        return showDetailLog;
    }

    public void setShowDetailLog(boolean showDetailLog) {
        this.showDetailLog = showDetailLog;
    }

    @Override
    public String toString() {
        return "ToolConfig{" +
                "tencentSid='" + tencentSid + '\'' +
                ", tencentSkey='" + tencentSkey + '\'' +
                ", keystoreFilePath='" + keystoreFilePath + '\'' +
                ", keyPassword='" + keyPassword + '\'' +
                ", storePassword='" + storePassword + '\'' +
                ", keyAlias='" + keyAlias + '\'' +
                ", targetApkPath='" + targetApkPath + '\'' +
                ", channelMask='" + channelMask + '\'' +
                ", channelConfigPath='" + channelConfigPath + '\'' +
                ", showDetailLog=" + showDetailLog +
                '}';
    }
}
