package kr.susemi99.jungnangwomen.extension

import java.io.InputStream

val InputStream.toString
  get() = String(readBytes())