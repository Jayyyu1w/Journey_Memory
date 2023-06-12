package com.example.Journey_Memory.data

data class CellPreserveData(val text: String?, val imageData: String?, val voiceData: String?,val locationData:String?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CellPreserveData

        if (text != other.text) return false
        if (imageData != other.imageData) return false
        if (locationData != other.locationData) return false
        if (voiceData != other.voiceData) return false
        /*if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false*/

        return true
    }

    /*override fun hashCode(): Int {
        var result = text?.hashCode() ?: 0
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }*/
}