@file:OptIn(ExperimentalSerializationApi::class)

package net.adarw.serialization.definitions

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
enum class WidgetType {
    @ProtoNumber(0) OBJ,
    @ProtoNumber(1) SCREEN,
    @ProtoNumber(2) BUTTON,
    @ProtoNumber(3) LABEL,
    @ProtoNumber(4) SLIDER,
    @ProtoNumber(5) CHECKBOX,
    @ProtoNumber(6) IMAGE
}

@Serializable
enum class AlignType {
    @ProtoNumber(0) DEFAULT,
    @ProtoNumber(1) CENTER,
    @ProtoNumber(2) TOP_LEFT,
    @ProtoNumber(3) BOTTOM_RIGHT
}

@Serializable
enum class EventType {
    @ProtoNumber(0) CLICKED,
    @ProtoNumber(1) LONG_PRESSED,
    @ProtoNumber(2) VALUE_CHANGED,
    @ProtoNumber(3) FOCUSED
}

@Serializable
enum class Side {
    @ProtoNumber(0) SIDE_ALL,
    @ProtoNumber(1) SIDE_TOP,
    @ProtoNumber(2) SIDE_BOTTOM,
    @ProtoNumber(3) SIDE_LEFT,
    @ProtoNumber(4) SIDE_RIGHT
}

@Serializable
data class Padding(
    @ProtoNumber(1) val side: Side = Side.SIDE_ALL,
    @ProtoNumber(2) val pad: Int = 0
)

@Serializable
data class Margin(
    @ProtoNumber(1) val side: Side = Side.SIDE_ALL,
    @ProtoNumber(2) val margin: Int = 0
)

@Serializable
data class Style(
    @ProtoNumber(1) val textColor: UInt = 0u,
    @ProtoNumber(2) val bgColor: UInt = 0u,
    @ProtoNumber(3) val borderColor: UInt = 0u,
    @ProtoNumber(4) val borderWidth: Int = 0,
    @ProtoNumber(5) val radius: Int = 0,
    @ProtoNumber(6) val paddings: List<Padding> = emptyList(),
    @ProtoNumber(7) val margins: List<Margin> = emptyList()
)

@Serializable
data class EventHandler(
    @ProtoNumber(1) val trigger: EventType = EventType.CLICKED,
    @ProtoNumber(2) val actionId: String = ""
)

@Serializable
data class WidgetData(
    @ProtoNumber(1) val sliderMin: Int = 0,
    @ProtoNumber(2) val sliderMax: Int = 0,
    @ProtoNumber(3) val sliderVal: Int = 0,
    @ProtoNumber(4) val isChecked: Boolean = false
)

@Serializable
data class LvObject(
    @ProtoNumber(1) val type: WidgetType = WidgetType.OBJ,
    @ProtoNumber(2) val id: String = "",
    @ProtoNumber(3) val x: Int = 0,
    @ProtoNumber(4) val y: Int = 0,
    @ProtoNumber(5) val width: Int = 0,
    @ProtoNumber(6) val height: Int = 0,
    @ProtoNumber(7) val align: AlignType = AlignType.DEFAULT,
    @ProtoNumber(8) val text: String = "",
    @ProtoNumber(9) val style: Style? = null,
    @ProtoNumber(10) val data: WidgetData? = null,
    @ProtoNumber(11) val events: List<EventHandler> = emptyList(),
    @ProtoNumber(12) val children: List<LvObject> = emptyList()
)