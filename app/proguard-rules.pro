# Keep the package name
-keeppackagenames com.eblan.launcher.domain.model.**

# Keep the enum classes and their members (values/names)
-keep enum com.eblan.launcher.domain.model.** {
    *;
}

# Exclude EblanApplicationInfos in VerticalApplicationScreen.kt from R8 optimization (prevents $changed2 int->byte narrowing)
# This bug is weird and confusing WTF!
-keep class com.eblan.launcher.feature.home.screen.application.vertical.VerticalApplicationScreenKt {
    void EblanApplicationInfos(...);
}