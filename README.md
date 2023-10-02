# Android an
Classical anti-mine that rewards users with pictures as prizes.

Work based on https://github.com/lucasnlm/antimine-android

## How to run

1. Import into android studio
2. Add `prize-images` directory in `app/src/main/assets` and some prize images inside
3. Update you app id for AdMob in: (AndroidManifest.xml)[proprietary/src/main/AndroidManifest.xml]
4. Update you ad ids for AdMob in: (Ads.kt)[external/src/main/java/dev/lucasnlm/external/Ads.kt]
5. Update local properties: 
   storeFile=*.jks
   keyAlias=dev
   storePassword=pass
   keyPassword=pass