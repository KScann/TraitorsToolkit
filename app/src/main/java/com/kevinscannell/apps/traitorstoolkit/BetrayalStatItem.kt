package com.kevinscannell.apps.traitorstoolkit

class BetrayalStatItem( upStats: Array<Int>, downStats: Array<Int>, newname : String, newtype : Int) {
    enum class ITEMTYPE{
        UNKNOWN,
        ITEM,
        OMEN
    }

    var upSpeed : Int = 0
    var upMight : Int = 0
    var upSanity : Int = 0
    var upKnowledge : Int = 0
    var downSpeed : Int = 0
    var downMight : Int = 0
    var downSanity : Int = 0
    var downKnowledge : Int = 0
    var name : String = ""
    var type : ITEMTYPE = ITEMTYPE.UNKNOWN

    init {
        upSpeed = upStats[0]
        upMight = upStats[1]
        upSanity = upStats[2]
        upKnowledge = upStats[3]

        downSpeed = downStats[0]
        downMight = downStats[1]
        downSanity = downStats[2]
        downKnowledge = downStats[3]

        name = newname
        type = ITEMTYPE.values()[newtype]
    }

}