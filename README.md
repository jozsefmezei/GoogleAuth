# About
Based on https://github.com/wstrange/GoogleAuth. This library written for Android Clients in Java 8. 

# Demo
![Main screen](/demo/device-screen.png)


# What is
 - TOTP password: The Time-based One-Time Password algorithm (TOTP) is an algorithm that computes a one-time password from a shared secret key and the current time.
 - Secret key: unique string, which known by the device and server as well

# Features:
 - token generating time by time
 - time synchronisation default implementation 
 - time correction in run time (after sync)
 - offline working
 - 6 or 8 digits support
 
 # Dependency
 
 Add the JitPack repository into your project level gradle:
 ```groovy
 allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
  }
  ```
  
 Add the dependency into your app level gradle:
  ```groovy
dependencies {
	      implementation 'com.github.jozsefmezei:GoogleAuth:1.3.6'
	}
```

# How to use
```kotlin
// Init ------------------
val config = GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setHmacHashFunction(HmacHashFunction.HmacSHA256)
                .setCodeDigits(6)
                .setWindowSize(3)
                .setEncodeType(BaseUtils.BaseType.BASE_32)
                .build()

var authenticator = GoogleAuthenticator(config)
authenticator.setSecureRandom(ReseedingSecureRandom(authenticator?.getRandomNumberAlgorithm()))
 
 // Start token generation       
authenticator.startTotpPasswordGeneration(this, twoaf, value)
 
// Listener --------------
   private val twoaf = object : Twoaf {
        override fun onTokenChangedListener(token: String?, remainingTimeInSeconds: Long) {
            // do somethnig on background
        }

        override fun onTokenChangedUIListener(token: String, remainingTimeInSeconds: Long) {
            // do somethnig on UI
        }
    }
```
For more sample please check the example application.

# Licence
```
Copyright 2018 József Mezei

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```