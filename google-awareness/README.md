# Description

This project is a sample app for [Google Awareness API](https://developers.google.com/awareness/).

The Awareness API unifies 7 location and context signals in a single API, enabling you to create powerful context-based features with minimal impact on system resources.  
Combine optimally processed context signals in new ways that were not previously possible, while letting the API manage system resources so your app doesn't have to.

# Required

## Get API Keys
You need to get Android API Keys from [Google API Console](https://console.developers.google.com/apis?project=profile-notification-95441&hl=JA).

How to get the API Keys is listed [here](https://developers.google.com/awareness/android-api/get-a-key)

## Enable API
You need to enable Google API(Google Awareness API, Google Maps Geocoding API, Nearby Messages API) from [Google API Console](https://console.developers.google.com/apis?project=profile-notification-95441&hl=JA).

How to enable the API is listed [here](https://developers.google.com/awareness/android-api/get-a-key#activate_additional_apis).

## Set up gradle.properties

This sample app needs to Android API Keys and attachment values of Google Beacons.
You must create ```gradle.properties``` file and set values into it.

```google-awareness/gradle.properties

# All keys may be the same.
awarenessApiKey = "awarenessApiKey"
geoApiKey = "geoApiKey"
nearbyMessagesApiKey = "nearbyMessagesApiKey"

# These values are attachment values of Google Beacons.
beaconAttachmentNamespace = "namesapce"
beaconAttachmentType = "type"

```


