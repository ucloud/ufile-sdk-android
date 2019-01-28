# Ufile SDK for Android

[![](https://img.shields.io/github/release/ucloud/ufile-sdk-java.svg)](https://github.com/ucloud/ufile-sdk-java)
[![](https://img.shields.io/github/last-commit/ucloud/ufile-sdk-java.svg)](https://github.com/ucloud/ufile-sdk-java)
[![](https://img.shields.io/github/commits-since/ucloud/ufile-sdk-java/latest.svg)](https://github.com/ucloud/ufile-sdk-java)

## 注意事项 (请仔细阅读)
- Ufile SDK for Android 与 Java版本共用一套。本仓库中项目代码仅为Android端Demo，使用的Ufile SDK是Ufile SDK for Java，请移步至 **[ufile-sdk-java](https://github.com/ucloud/ufile-sdk-java)**
- Android端接入Ufile SDK涉及到秘钥安全性问题，所以请务必遵守以下规则
    - 签名器接口请实现**ObjectRemoteAuthorization**或者使用已经实现好的**UfileObjectRemoteAuthorization**类，ObjectRemoteAuthorization接口只在本地放入公钥，私钥需要放在签名服务器中，通过接口计算签名。
    - Android端请不要使用Bucket相关API，因Bucket相关API的签名无法远程签名
- 远程签名服务源码，请移步至 **[ufile-sdk-auth-server](https://github.com/ucloud/ufile-sdk-auth-server)**

## 环境要求
- JDK: 1.8或以上
- Android Demo: API Level 14 (Android 4.0) 或以上

## 快速安装
您可以clone源码后，在Android Studio中编译并打包安装，也可以直接下载[UfileSdkDemo.apk](http://ucloud-apk.cn-sh2.ufileos.com/UfileSdkDemo.apk)安装

## License
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)

## 作者
- [Joshua Yin](https://github.com/joshuayin)

