[Навигация по SRSPU]

`D:\Documents\SDK` - SDK
`B:\JDK24` - JDK / JAVA_HOME
`C:\Users\Den\AppData\Local\Android\Sdk` - SDK AndroidStudio  / ANDROID_HOME

Как собрать APK?
В папке проекта 
`./gradlew assembleDebug`

Установите APK на устройство:

`adb install D:\Documents\SRSPU_NAVIGATION\app\build\outputs\apk\debug\app-debug.apk`

Для переустановки добавляем `-r`:
`adb install -r D:\Documents\SRSPU_NAVIGATION\app\build\outputs\apk\debug\app-debug.apk`