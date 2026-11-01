# Add project specific ProGuard rules here.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keepclassmembers class * {
    public <init>(android.content.Context);
}
