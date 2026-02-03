package org.example.project

import java.util.Locale

fun getDefaultLocal() : String {
    return Locale.getDefault().language.toString()
}