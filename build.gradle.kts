// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    // --- УБРАНО: Объявление плагина Hilt с версией ---
    // id("com.google.dagger.hilt.android") version "2.56.2" apply false
    // --- КОНЕЦ УБРАНО ---
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}