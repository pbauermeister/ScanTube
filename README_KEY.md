# Handling Keys

Several API keys are needed for this project. The keys must be protected. Compromised keys could mean unwanted API calls can be made at your costs.

## A. Google API key for access to YouTube

1. To obtain an API key, visit https://console.developers.google.com/apis
    1. Select or create your project
    2. Add API for: YouTube Data API v3
    3. Return to the developer dashboard and select credentials: https://console.developers.google.com/apis/credentials
    4. Create credentials of type API key
    5. Copy or note the key value    

## B. Google API key for access to Cloud Vision

1. You can use the same key as above, or create a separate key
   (in the latter case, create an additional key in the Google developer console; you may want to
   restrict each key to a separate Google service)

2. Once you have a key, store it in the same `gradle.properties` file, as the line:

   ``SCANTUBE_GOOGLEAPIS_CLOUDVISION_APPKEY="your-key-here"``

3. This API is subject to costs. You will have to enable payment methods in the Google developer console.

## C. Bringing the keys into the app

### Variant 1: let user enter the keys via preferences

- Pros: If you share your app, all users will NOT consume your quotas on the Google services.
- Cons: Users must create themselves API keys and enter them into the preferences.

This variant is currently implemented in the app.

### Variant 2: embed the keys in your app as constants.

- Pros: you will never have to enter it manually.
- Cons: If you share your app, all users will consume your quotas on the Google services.

This variant was implemented in the first git revisions. It has been replaced by variant 1. To modify the app to use variant 2, please consider the following:

1. To store the key in your computer, outside the source code repository, edit/create this file:

   ``<my-home-dir>/.gradle/gradle.properties``
   
   Attention: you are responsible for the security and backup of this location!

2. In this `gradle.properties` file, have the line:

   ``SCANTUBE_GOOGLEAPIS_YOUTUBE_APPKEY="your-key-here"``
 
3. In the file YouTubeService.kt, have:

   ``const val APP_KEY = BuildConfig.SCANTUBE_GOOGLEAPIS_YOUTUBE_APPKEY``

4. Do the same for the Google Cloud Vision key.


## References

- https://medium.com/code-better/hiding-api-keys-from-your-android-repository-b23f5598b906
