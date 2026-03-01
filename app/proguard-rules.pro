# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.stocktracker.data.api.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
