package ru.icames.store.util

enum class DeviceType(val modelName: String) {
    SMART_LITE("АТОЛ Smart.Lite"),
    SMART_DROID("АТОЛ Smart.Droid"),
    SMARTPHONE("Смартфон");

    override fun toString(): String {
        return modelName
    }
}