package kr.susemi99.jungnangwomen.util

import timber.log.Timber

class DebugLogTree : Timber.DebugTree() {
  override fun createStackElementTag(element: StackTraceElement) = "App: (${element.fileName}:${element.lineNumber})"
}
