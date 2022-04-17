package kr.susemi99.jungnangwomen.extension

import java.text.NumberFormat

val Float.comma: String
  get() = NumberFormat.getIntegerInstance().format(this)