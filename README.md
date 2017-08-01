# Bbox Voicemail API #

[![Build Status](https://travis-ci.org/bertrandmartel/bboxapi-voicemail.svg)](https://travis-ci.org/bertrandmartel/bboxapi-voicemail)
[![Download](https://api.bintray.com/packages/bertrandmartel/maven/bboxapi-voicemail/images/download.svg) ](https://bintray.com/bertrandmartel/maven/bboxapi-voicemail/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.bmartel/bboxapi-voicemail/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.bmartel/bboxapi-voicemail)
[![Javadoc](http://javadoc-badge.appspot.com/fr.bmartel/bboxapi-voicemail.svg?label=javadoc)](http://javadoc-badge.appspot.com/fr.bmartel/bboxapi-voicemail)
[![License](http://img.shields.io/:license-mit-blue.svg)](LICENSE.md)

**Experimental** Java/Android client library for unofficial [Bbox VoiceMail API](https://www.messagerievocale.bbox.bouyguestelecom.fr/)

Most of these API are using web scraping. Page content may change in the future so it can be deprecated at any time. [Create an issue](https://github.com/bertrandmartel/bboxapi-voicemail/issues/new) if you notice something broken. 

## API List

| description     | api          |       
|--------------|---------|
| get welcome message  | [`getWelcomeMessage(int id, String fileDest)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/request/GetWelcomeMessage.java) | 
| upload welcome message  | [`uploadWelcomeMessage(String filePath, int messageId, int selectedId)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/action/UploadWelcomeMessage.java) |  
| set dual call state | [`setDualCallState(DualCallState state)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/action/SetDualCallState.java) |  
| set incognito mode | [`setIncognitoMode(IncognitoState state)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/action/SetIncognitoState.java) | 
| set SMS notification | [`setVoiceMailSMS(NotifState voiceMail, NotifState missedCall, String number)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/action/SetSmsNotification.java) |
| set voiceMail state | [`setVoiceMailState(VoiceMailState state, int ringNumber)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/action/SetVoiceMailState.java) |
| set welcome message state | [`setWelcomeMessageState(WelcomeMessageState state)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/action/SetWelcomeMessageState.java) | 

**Note**: If you need to list voicemail, read voicemail, get voicemail URL, forward call, use [official Bbox Router API](https://api.bbox.fr/doc/apirouter/index.html) with [Java/Android client for Bbox Router API](https://github.com/bertrandmartel/bboxapi-router)

All APIs need authentication via : 

```java
VoiceMailApi api = new VoiceMailApi();

ApiResponse loginResponse = api.login("username@bbox.fr", "password");
```

This will only work with your `@bbox` account (no such service for mobile)

## Include into your project

* with Gradle, from JCenter or MavenCentral :

```java
compile 'fr.bmartel:bboxapi-voicemail:1.0.0'
```

## Usage

### Get welcome message

This will store the welcome message to a local file. The following will store the welcome message with id 1 under `/home/user/message.wav` : 

```java
ApiResponse response = api.getWelcomeMessage(1, "/home/user/message.wav");
```

### Upload welcome message

The following will upload `/home/user/message2.mp3` as the welcome message with id 2 and select the welcome message with id 2 :

```java
ApiResponse response = api.uploadWelcomeMessage("/home/user/message2.mp3", 2, 2);
```

### Set dual call state

```java
ApiResponse response = api.setDualCallState(DualCallState.ENABLE);
```

### Set incognito mode

```java
ApiResponse response = api.setIncognitoMode(IncognitoState.DISABLE);
```

### Set SMS notification

enable SMS notification to specified phone number on new voicemail and on missed call : 
```java
ApiResponse response = api.setVoiceMailSMS(NotificationState.ENABLE, NotificationState.ENABLE, "0123456789");
```

### Set voicemail state

enable/disable voicemail and set the ring number (default 5)

```java
ApiResponse response = api.setVoiceMailState(VoiceMailState.ENABLE, 5);
```

### Set welcome message state

enable/disable welcome message : 

```java
ApiResponse response = api.setWelcomeMessageState(WelcomeMessageState.ENABLE);
```

## Other APIs

For reference, the following API are implemented : 

| description     | api          |       
|--------------|---------|
| get voicemail list | [`getVoiceMailList()`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/request/GetVoiceMailList.java) |
| delete voicemail | [`deleteVoiceMail(int id)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/action/DeleteVoiceMail.java) |   
| get user info | [`getCustomerInfo()`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/request/GetUserInfo.java) | 
| forward call | [`setCallForwarding(CallForwardType type, String phoneNumber)`](./examples/src/main/java/fr/bmartel/bboxapi/voicemail/examples/action/SetForwardCall.java) | 

**Don't use these API above**, use [official Bbox Router API](https://api.bbox.fr/doc/apirouter/index.html) instead with [Java/Android client for Bbox Router API](https://github.com/bertrandmartel/bboxapi-router)

### Get voicemail list 

```java
VoiceMailResponse voiceMailResponse = api.getVoiceMailList();

if (voiceMailResponse.getStatus() == HttpStatus.OK) {

    List<VoiceMail> voiceMailList = voiceMailResponse.getVoiceMailList();
}
```

### Delete voicemail

```java
ApiResponse response = api.deleteVoiceMail("1234567");
```

### Get user info

```java
UserInfo userInfo = api.getCustomerInfo();
```

### Forward call

disable forward call

```java
ApiResponse response = api.setCallForwarding(CallForwardType.DISABLE, "");
```

forward all call to voicemail : 

```java
ApiResponse response = api.setCallForwarding(CallForwardType.ALL_TO_VOICEMAIL, "");
```

forward all call to phone number : 

```java
ApiResponse response = api.setCallForwarding(CallForwardType.ALL_TO_NUMBER, "0123456789");
```

forward unanswered call to phone number : 

```java
ApiResponse response = api.setCallForwarding(CallForwardType.UNANSWERED_CALL_TO_NUMBER, "0123456789");
```

forward call to phone number when line is occupied : 

```java
ApiResponse response = api.setCallForwarding(CallForwardType.LINE_OCCUPIED_TO_NUMBER, "0123456789");
```

forward call to pĥone number when line is unavailable : 

```java
ApiResponse response = api.setCallForwarding(CallForwardType.LINE_UNAVAILABLE_TO_NUMBER, "0123456789");
```

## Android integration

* add `bboxapi-voicemail` & `httpcomponents` lib depedency to `build.gradle` : 

```java
compile 'org.apache.httpcomponents:httpclient-android:4.3.5.1'
compile 'fr.bmartel:bboxapi-voicemail:1.0.0'
```

* add Internet permission to manifest :

```java
<uses-permission android:name="android.permission.INTERNET" />
```

* proguard config (keep model & response packages) :

```
-keep class fr.bmartel.bboxapi.voicemail.model.** { *; }
-keep class fr.bmartel.bboxapi.voicemail.response.** { *; }
```

## External Library

* [Apache HttpComponents](https://hc.apache.org)
* [JSoup](https://jsoup.org/)

## License

The MIT License (MIT) Copyright (c) 2017 Bertrand Martel
