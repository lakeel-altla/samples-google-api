# Description

A sample for Nearby Messages.  

The Nearby Messages API is a publish-subscribe API that lets you pass small binary payloads between internet-connected Android and iOS devices.  
The devices don't have to be on the same network, but they do have to be connected to the Internet.

By using the API, you can get the attachment data of the beacon.

# Required

You need to register beacon to Google by using [Proximity Beacon API](https://developers.google.com/beacons/proximity/guides).

Beacon that has been registered you can check on [Google Beacon Dashboard](https://developers.google.com/beacons/dashboard).

You need to get your Android API key associated with google project that registered the beacons.
After getting the API key, paste it in ```AndroidManifest.xml```.  
[How to get API key](https://developers.google.com/maps/documentation/android-api/signup)