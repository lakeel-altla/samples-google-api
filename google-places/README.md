# Description

This project is a sample app for Google Places.

By using the Google places API, your app will be able to access with detailed information about 100 million places across a wide range of categories, from the same database as Google Maps and Google+.

# Required

## Get API Key

You need to create a Google project to get your API key.
After Getting the API key, paste it in ```AndroidManifest.xml```.

[How to get API key](https://developers.google.com/places/android-api/signup)

## Enable API
You need to enable Google Places API from [Google API Console](https://console.developers.google.com/apis?project=profile-notification-95441&hl=JA).

How to enable the API is listed [here](https://developers.google.com/awareness/android-api/get-a-key#activate_additional_apis).

## Set up gradle.properties

This sample app needs to Android API Key for Google Place.
You must create ```gradle.properties``` file and set values into it.

```google-places/gradle.properties

# API Key
GEO_API_KEY = "GEO_API_KEY"

```
