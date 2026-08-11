# Android an
Classical anti-mine that rewards users with pictures as prizes.

Work based on https://github.com/lucasnlm/antimine-android

## How to run

1. Clone [release-tools](https://github.com/robmat/release-tools) as a sibling directory (`git clone https://github.com/robmat/release-tools.git ../release-tools`) — this repo's Gradle build pulls shared build logic and the version catalog from it via `includeBuild`.
2. Import into android studio
3. Add `prize-images` directory in `app/src/main/assets` and some prize images inside
4. Update you app id for AdMob in: (AndroidManifest.xml)[proprietary/src/main/AndroidManifest.xml]
5. Update you ad ids for AdMob in: (Ads.kt)[external/src/main/java/dev/lucasnlm/external/Ads.kt]
6. Update local properties: 
   storeFile=*.jks
   keyAlias=dev
   storePassword=pass
   keyPassword=pass