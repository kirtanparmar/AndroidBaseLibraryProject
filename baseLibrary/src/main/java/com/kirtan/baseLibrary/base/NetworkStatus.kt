package com.kirtan.baseLibrary.base

sealed class NetworkStatus {
    object NetworkConnected : NetworkStatus()
    object NetworkDisconnected : NetworkStatus()
}