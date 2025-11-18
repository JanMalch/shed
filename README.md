# Shed

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.janmalch/shed)](https://central.sonatype.com/artifact/io.github.janmalch/shed)

_Putting [Timber](https://github.com/JakeWharton/timber) into a [Room](https://developer.android.com/training/data-storage/room)._

## About

Shed persists your `Timber` logs in a database, and provides an Activity to [view](#screenshots) and export them.

Use it for hobby projects or internal apps, without access to a proper remote logging system.
You most likely don't want to use it in a real production app.

## Installation

At its core, Shed provides two modules: `shed` and `shed-nop`.
`shed-nop` has the same API surface as `shed`, but its implementations do nothing at runtime.

```kotlin
dependencies {
    val shed_version = "0.4.1"
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Only use database in debug builds, do nothing in release builds.
    debugImplementation("io.github.janmalch:shed:$shed_version")
    releaseImplementation("io.github.janmalch:shed-nop:$shed_version")
}
```

You can then create and plant a tree, just like any other `Timber.Tree`.

### Auto-Loading

The `shed-autoload` module provides a boilerplate-free way to plant a tree with default options.

```kotlin
dependencies {
    debugImplementation("io.github.janmalch:shed:$shed_version")
    debugImplementation("io.github.janmalch:shed-autoload:$shed_version")
    releaseImplementation("io.github.janmalch:shed-nop:$shed_version")
}
```

Under the hood, it uses a `ContentProvider` which is merged into your app's manifest.
Thus, this module has [no public API](./shed-autoload/api/shed-autoload.api) 
and simply doesn't need a `releaseImplementation` equivalent.

This convenience comes at the cost of not being able to configure the tree.
When using autoload, all log entries are persisted for 3 days.

### Manual Configuration

When planting the tree manually, you have control over several options, like filtering logs
and more precise clean-up parameters. Please refer to [its documentation](https://janmalch.github.io/shed/shed/io.github.janmalch.shed/-shed/create-tree.html)
for more info.

```kotlin
class ShedDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            // When using "shed-nop", this factory returns a no-op tree.
            Timber.plant(Shed.createTree(context = this, keepLatest = 1_000))
        }
    }
}
```

## Usage

To view and export the logs, you can simply navigate to the dedicated activity
with the static [`Shed.startActivity` function](./shed/src/main/java/io/github/janmalch/shed/Shed.kt#L50).
For `shed-nop`, this will be a [no-op call](./shed-nop/src/main/java/io/github/janmalch/shed/Shed.kt#L45).

```kotlin
Button(
    onClick = { Shed.startActivity(context) }
) {
    Text("View Logs")
}
```

See the [demo app](./app/src/main/java/com/example/app) for a full setup.

## Screenshots

![demo](./.github/assets/demo.png)
