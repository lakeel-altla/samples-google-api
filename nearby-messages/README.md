# Description

A sample for Nearby Messages.  

The Nearby Messages API is a publish-subscribe API that lets you pass small binary payloads between internet-connected Android and iOS devices.  
The devices don't have to be on the same network, but they do have to be connected to the Internet.

By using the API, you can get the attachment data of the beacon.

# Required

## Get API Key

You need to get Android API Keys from [Google API Console](https://console.developers.google.com/apis?project=profile-notification-95441&hl=JA).

How to get the API Keys is listed [here](https://developers.google.com/awareness/android-api/get-a-key)

## Enable API
You need to enable Nearby Messages API from [Google API Console](https://console.developers.google.com/apis?project=profile-notification-95441&hl=JA).

How to enable the API is listed [here](https://developers.google.com/awareness/android-api/get-a-key#activate_additional_apis).

## Register Beacon

You need to register beacon to Google by using [Proximity Beacon API](https://developers.google.com/beacons/proximity/guides).

Beacon that has been registered you can check on [Google Beacon Dashboard](https://developers.google.com/beacons/dashboard).

## Set up gradle.properties
   
This sample app needs to Android API Keys and attachment values of Google Beacons.  
You must create ```gradle.properties``` file and set values into it.

```naerby-messages/gradle.properties
    
    # Not need to double quotation.
    NEARBY_MESSAGES_API_KEY = NEARBY_MESSAGES_API_KEY
    
    # NOTE: Double quotation is needed.
    BEACON_ATTACHMENT_NAMESPACE = "BEACON_ATTACHMENT_NAMESPACE"
    
```