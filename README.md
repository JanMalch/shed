# Shed

_Putting [Timber](https://github.com/JakeWharton/timber) into a [Room](https://developer.android.com/training/data-storage/room)._

## About

Shed is all about persisting your `Timber` logs in a database,
in case you don't have a remote logging system.
Thus, making it useful for internal apps or hobby projects. 
You most likely don't want to have it in a real production app.


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

Shed is split into multiple modules. Most likely, you are only concerned with `shed` and `shed-nop`.
`shed-nop` has the same API surface as `shed`, but its implementations do nothing at runtime.

```kotlin
dependencies {
    val shed_version = "?"
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // only use database in debug builds, do nothing in release builds
    debugImplementation("com.github.JanMalch:shed:$shed_version")
    releaseImplementation("com.github.JanMalch:shed-nop:$shed_version")
}
```

You can then plant a `Shed.Tree` just like any other `Timber.Tree`.

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

The `shed` module also provides a `ShedActivity` that will display your log entries from the database.
It also offers a way to export the entire log as a JSON file.
It's **highly recommended** to start the activity via its static [`start` function](./shed/src/main/java/io/github/janmalch/shed/ShedActivity.kt#L262),
so that `shed-nop` can replace with a simple [no-op call](./shed-nop/src/main/java/io/github/janmalch/shed/ShedActivity.kt#L18).

```kotlin
Button(
    onClick = { ShedActivity.start(context) }
) {
    Text("View Logs")
}
```

See the [demo app](./app/src/main/java/io/github/janmalch/shed) for a full setup.

### Advanced

In case you want more flexibility, you can depend on the `shed-database` and `shed-tree` modules
to gain access to the underlying classes.
