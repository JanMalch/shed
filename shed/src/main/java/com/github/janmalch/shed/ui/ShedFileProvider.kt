package com.github.janmalch.shed.ui

import androidx.core.content.FileProvider

/**
 * Custom [FileProvider] to prevent Manifest issues.
 *
 * See [FileProvider and Libraries](https://commonsware.com/blog/2017/06/27/fileprovider-libraries.html).
 */
internal class ShedFileProvider : FileProvider()
