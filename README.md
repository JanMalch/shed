# Shed

_Putting [Timber](https://github.com/JakeWharton/timber) into a [Room](https://developer.android.com/training/data-storage/room)._

## About

Shed persists your `Timber` logs in a database, and provides an Activity to view and export them.

Use it for hobby projects or internal apps, without access to a proper remote logging system. 
You most likely don't want to use it in a real production app.

## Installation

Currently, Shed is only available via [JitPack](https://jitpack.io/).
I would recommend adding JitPack only for specific projects.

```kotlin
repositories {
    // other repositories like google() and mavenCentral() ...
    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://jitpack.io")
            }
        }
        filter {
            includeModuleByRegex("com.github.JanMalch", "shed.*")
        }
    }
}
```

Shed provides two modules: `shed` and `shed-nop`.
`shed-nop` has the same API surface as `shed`, but its implementations do nothing at runtime.

```kotlin
dependencies {
    val shed_version = "?"
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Only use database in debug builds, do nothing in release builds.
    debugImplementation("com.github.JanMalch:shed:$shed_version")
    releaseImplementation("com.github.JanMalch:shed-nop:$shed_version")
}
```

You can then create and plant a tree, just like any other `Timber.Tree`.

```kotlin
class ShedDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            // When using "shed-nop", this factory returns a no-op tree.
            Timber.plant(Shed.createTree(context = this))
        }
    }
}
```

The `Shed.createTree` factory also provides optional clean-up parameters,
so that the database doesn't grow forever.
Please refer to its documentation for more info.

To view and export the logs, you can simply navigate to the dedicated activity
with the static [`Shed.startActivity` function](./shed/src/main/java/io/github/janmalch/shed/Shed.kt#L28).
For `shed-nop`, this will be a [no-op call](./shed-nop/src/main/java/io/github/janmalch/shed/Shed.kt#L23).

```kotlin
Button(
    onClick = { Shed.startActivity(context) }
) {
    Text("View Logs")
}
```

See the [demo app](./app/src/main/java/com/example/app) for a full setup.
