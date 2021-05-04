<div style="width:100%">
    <div style="width:50%; display:inline-block">
        <p align="center">
        <img align="center" alt="" src="https://avatars2.githubusercontent.com/u/45484907?s=200&v=4">
        </p>
    </div>
</div>

<br></br><br></br>

# Android Kotlin Chat UI Kit

CometChat Kotlin UI Kit is a collection of custom UI Components designed to build text chat and voice/video callings features in your application. 
The UI Kit is developed to keep developers in mind and aims to reduce development efforts significantly.

</br></br>

![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![Platform](https://img.shields.io/badge/Language-Kotlin-yellowgreen.svg)
![GitHub repo size](https://img.shields.io/github/repo-size/cometchat-pro/android-kotlin-chat-ui-kit)
![GitHub contributors](https://img.shields.io/github/contributors/cometchat-pro/android-kotlin-chat-ui-kit)
![GitHub stars](https://img.shields.io/github/stars/cometchat-pro/android-kotlin-chat-ui-kit?style=social)
![Twitter Follow](https://img.shields.io/twitter/follow/cometchat?style=social)

---

## Prerequisites :star:
Before you begin, ensure you have met the following requirements:<br/>
✅ &nbsp; You have `Android Studio` installed in your machine <br/>
✅ &nbsp; Android kotlin Chat UI Kit works for the Android devices from Android 6.0 and above <br/>
✅ &nbsp; You have read [CometChat Key Concepts](https://prodocs.cometchat.com/docs/concepts).<br/>

---

## Installing Android Kotlin Chat UI Kit 
## Setup :wrench:
To install Android Kotlin UI Kit, you need to first register on CometChat Dashboard. [Click here to sign up](https://app.cometchat.com/login).

###  i. Get your Application Keys :key:
- Create a new app: Click **Add App** option available  →  Enter App Name & other information  → Create App
- You will find `APP_ID`, `API_KEY` and `REGION` key at top in **QuickStart** section or else go to "API & Auth Keys" section and copy the `APP_ID`, `API_KEY` and `REGION` key from the "Auth Only API Key" tab.
<img align="center" src="https://github.com/cometchat-pro-samples/android-kotlin-chat-app/blob/master/Screenshot/qs.png"/>


---

###  ii. Add the CometChat Dependency

First, add the repository URL to the project level build.gradle file in the repositories block under the allprojects section.
```groovy
allprojects {
	repositories {
		maven {
			url "https://dl.cloudsmith.io/public/cometchat/cometchat-pro-android/maven/"
		}
	}
}
```

Then, add CometChat to the app level build.gradle file in the dependencies section.
```groovy
dependencies {
	implementation 'com.cometchat:pro-android-chat-sdk:2.3.1'
}
```
```groovy
android {
	defaultConfig {
		manifestPlaceholders = [file_provider: "YOUR_PACKAGE_NAME"] 
		//add your application package.
	}
}
```

As the UI Kit uses dataBinding you must enable dataBinding to use UI Kit. To configure your app to use data binding, add the dataBinding element to your build.gradle file in the app module, as shown in the following example:
```groovy
android {
	...
	dataBinding {
		enabled = true
	}
}
```
Finally, add the below lines android section of the app level gradle file
```groovy
android {
	compileOptions {
		sourceCompatibility JavaVersion.VERSION_1_8
		targetCompatibility JavaVersion.VERSION_1_8
	}
}
```
## Configure CometChat inside your app
### i. Initialize CometChat
The `init()` method initializes the settings required for CometChat. We suggest calling the init() method on app startup, preferably in the onCreate() method of the Application class.

```kotlin
private val appID = "APP_ID"
private val region = "REGION"
val appSettings = AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(region).build()
			
CometChat.init(this, appID, appSettings, object : CallbackListener<String>() {
    override fun onSuccess(successMessage: String) {
        Log.d(TAG, "Initialization completed successfully")
    }

    override fun onError(e: CometChatException) {
         Log.d(TAG, "Initialization failed with exception: "+e.message)
    }
})
```

:information_source: &nbsp; **Note :
Make sure you replace the APP_ID with your CometChat `APP ID` and REGION with your app's `REGION` in the above code.**

---

### ii.Log in your User :user:
The `login()` method returns the User object containing all the information of the logged-in user.

```kotlin
private val UID = "SUPERHERO1"
private val AUTH_KEY = "Enter AUTH_KEY"
if (CometChat.getLoggedInUser() == null) {
    CometChat.login(UID, AUTH_KEY, object : CallbackListener<User?>() {
        override fun onSuccess(user: User?) {
		Log.d(TAG, "Login Successful : "+user.toString())
    }

        override fun onError(e: CometChatException) {
		Log.d(TAG, "Login failed with exception: " + e.message);
        }
    })
} else {
  // User already logged in
}
```
:information_source: &nbsp; **Note :**
* The login() method needs to be called only once.
* Make sure you replace the `AUTH_KEY` with your CometChat AUTH Key in the above code.
* We have setup 5 users for testing having UIDs: `SUPERHERO1`, `SUPERHERO2`, `SUPERHERO3`, `SUPERHERO4` and `SUPERHERO5`.

---


## Add UI Kit Library to your project
To integrate CometChat kotlin UIKit inside your app. Kindly follow the below steps:

1. Simply clone the UI Kit-Kotlin Library from [android-kotlin-chat-uikit](https://github.com/cometchat-pro/android-kotlin-chat-ui-kit) repository.
2. Import uikit-kotlin Module from Module Settings. ( To know how to import `uikit-kotlin` as Module visit this [link](https://prodocs.cometchat.com/docs/android-ui-kit-setup) )
3. If the Library is added successfully, it will look like mentioned in the below image.

<img align="center" width="auto" height="auto" src="https://github.com/cometchat-pro-samples/android-kotlin-chat-app/blob/master/Screenshot/file_structure.png">

4. Next steps is to adding necessary dependancies inside your app to integrate UI Kit-Kotlin.

* To use UI Kit-Kotlin you have to add Material Design Dependency as the UI Kit uses Material Design Components.
```groovy
implementation 'com.google.android.material:material:<version>'
```

:information_source: &nbsp; **Note :**
* As **UI Kit-Kotlin** is using material components your app's theme should extend `Theme.MaterialComponents`. Follow the guide on [Getting started Material Components](https://material.io/develop/android/docs/getting-started)

The following is the list of Material Components themes you can use to get the latest component styles and theme-level attributes.

`Theme.MaterialComponents` </br>
`Theme.MaterialComponents.NoActionBar`  </br>
`Theme.MaterialComponents.Light` </br>
`Theme.MaterialComponents.Light.NoActionBar` </br>
`Theme.MaterialComponents.Light.DarkActionBar` </br>
`Theme.MaterialComponents.DayNight` </br>
`Theme.MaterialComponents.DayNight.NoActionBar` </br>
`Theme.MaterialComponents.DayNight.DarkActionBar` </br>

Update your app theme to inherit from one of these themes, e.g.:
```xml
<style name="AppTheme" parent="Theme.MaterialComponents.Light.NoActionBar.Bridge">

    <!-- Customize your theme here. -->

</style>
```

## Launch UI Unified
**UI Unified** is a way to launch a fully working chat application using the **CometChat Kitchen Sink**. In UI Unified all the UI Screens and UI Components working together to give the full experience of a chat application with minimal coding effort.  

To use Unified UI user has to launch `CometChatUnified` class

Add the following code snippet in onSuccess of CometChat login.
```kotlin
startActivity(Intent(this@YourActivity, CometChatUnified::class.java))
```
<img align="center" width="100%" height="auto" src="https://github.com/cometchat-pro/android-kotlin-chat-app/blob/master/Screenshot/UI%20Unified.png">

---

## Checkout our sample apps
### Kotlin:
Visit our [Kotlin sample app](https://github.com/cometchat-pro/android-kotlin-chat-app) repo to run the kotlin sample app.
### Java:
Visit our [Java sample app](https://github.com/cometchat-pro/android-java-chat-app) repo to run the Java sample app.

## Troubleshooting
- To read the full documentation on UI Kit integration visit our [Documentation](https://prodocs.cometchat.com/docs/android-kotlin-ui-kit).
- Facing any issues while integrating or installing the UI Kit please <a href="https://app.cometchat.io/"> connect with us via real time support present in CometChat Dashboard.</a>.

## Contributors
Thanks to the following people who have contributed to this project:<br/>
[@poojashivane](https://github.com/PoojaShivane)

		
## :mailbox: Contact 
Contact us via real time support present in [CometChat Dashboard](https://app.cometchat.io/).

## License

This project uses the following license: [License](https://github.com/cometchat-pro/.github/blob/master/LICENSE).


