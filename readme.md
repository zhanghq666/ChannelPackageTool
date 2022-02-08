### 加固打包工具使用说明
1. 工具基于乐固加固，使用jarsigner对apk重签名，使用zipalign对apk对齐。
所以如果要使用加固功能，必须先在腾讯云注册账号并配置到配置文件中。
使用签名则必须配置JAVA_HOME/bin到Path中。使用zipalign则本地必须有安卓SDK且必须配置有ANDROID_HOME环境变量，确保本工具可以访问到zipalign工具。
使用举例：java -jar channel_tool.jar -i D:/config.ini -a target.apk
2. 使用必须配置好文档，配置文档字段说明如下：
```
[LeGu Config]
TencentSid=乐固Sid，去腾讯云注册账号即可获得
TencentSkey=乐固Skey，去腾讯云注册账号即可获得

[Keystore Config]
KeystoreFilePath=apk签名文件路径
KeyPassword=不谈
StorePassword=不谈
KeyAlias=不谈

[Channel Config]
ChannelMask=要修改的meta-data的android:name值，如CHANNEL_CODE
ChannelConfigPath=渠道配置csv文件路径
```
3. 渠道配置说明。渠道配置为一个csv文件，每一行为一个渠道，值为要修改的以ChannelMask为name的meta-data中的android:value的值。例如：
```
10001
10002
10003
10004
```
或
```
official_10001
yingyongbao_10002
oppo_10003
vivo_10004
```