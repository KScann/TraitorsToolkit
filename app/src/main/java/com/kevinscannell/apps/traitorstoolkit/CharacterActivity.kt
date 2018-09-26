package com.kevinscannell.apps.traitorstoolkit

import android.content.DialogInterface
import android.graphics.LightingColorFilter
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log.i
import android.view.View
import com.kevinscannell.apps.traitorstoolkit.R.id.btn_cv_speed_up
import com.kevinscannell.apps.traitorstoolkit.R.string.amulet
import kotlinx.android.synthetic.main.activity_character.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.InputStreamReader
import javax.xml.parsers.DocumentBuilderFactory

class CharacterActivity : AppCompatActivity() {

    companion object {
       const val CHARACTER_CODE = "character_code"
    }

    enum class ITEM_IDS{
        AMULET,
        BELL,
        BOOK,
        DOG,
        GIRL,
        SYMBOL,
        LOCKET,
        MADMAN
    }

    private val speedArray : Array<Int?> = arrayOfNulls(8)
    private val mightArray : Array<Int?> = arrayOfNulls(8)
    private val sanityArray : Array<Int?> = arrayOfNulls(8)
    private val knowledgeArray : Array<Int?> = arrayOfNulls(8)
    private val inventory : Array<Boolean> = Array(8, {i->false})
    private val itemColor : Int = 0xFFC37E2D.toInt()
    private val omenColor : Int = 0xFF3C6024.toInt()
    private val colorMul : Int = 0xFF000000.toInt()

    private var mCharacterCode = ""
    private var baseSpeed = 0
    private var baseMight = 0
    private var baseSanity = 0
    private var baseKnowledge = 0
    private var currentSpeed = 0
    private var currentMight = 0
    private var currentSanity = 0
    private var currentKnowledge = 0
    private var isDead : Boolean = false
    private var items : HashMap<String, BetrayalStatItem> = HashMap(8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)
        mCharacterCode = intent.getStringExtra(CHARACTER_CODE)
        when(mCharacterCode){
            "fr" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorBlack))
            "pl" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorBlack))
            "dw" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorRed))
            "ob" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorRed))
            "md" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorOrange))
            "zi" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorOrange))
            "pa" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorGreen))
            "bj" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorGreen))
            "vl" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorBlue))
            "mz" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorBlue))
            "hg" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorPurple))
            "jl" -> cl_character_view_root.setBackgroundColor(resources.getColor(R.color.characterColorPurple))
        }

        //Get appropriate arrays:
        val speedArrayString : String = getStringResourceByName("${mCharacterCode}_speed")
        val mightArrayString : String = getStringResourceByName("${mCharacterCode}_might")
        val sanityArrayString : String = getStringResourceByName("${mCharacterCode}_sanity")
        val knowledgeArrayString : String = getStringResourceByName("${mCharacterCode}_knowledge")
        val baseStatArrayString : String = getStringResourceByName("${mCharacterCode}_basestats")

        //Break them up
        val tSpeedArray : List<String> = speedArrayString.split(",")
        val tMightArray : List<String> = mightArrayString.split(",")
        val tSanityArray : List<String> = sanityArrayString.split(",")
        val tKnowledgeArray : List<String> = knowledgeArrayString.split(",")

        //Store them
        for (i in tSpeedArray.indices ){
            speedArray[i] = tSpeedArray[i].toInt()
            mightArray[i] = tMightArray[i].toInt()
            sanityArray[i] = tSanityArray[i].toInt()
            knowledgeArray[i] = tKnowledgeArray[i].toInt()
        }

        //Set health(s)
        val tStatArray : List<String> = baseStatArrayString.split(",")
        if( tStatArray.size < 4 ){
            return
        }

        baseSpeed = tStatArray[0].toInt()
        baseMight = tStatArray[1].toInt()
        baseSanity = tStatArray[2].toInt()
        baseKnowledge = tStatArray[3].toInt()

        this.resetCharacter()

        //Set fluff:
        tv_cv_name.text = getStringResourceByName("${mCharacterCode}_name")
        tv_cv_age.text = String.format("AGE: %s", getStringResourceByName("${mCharacterCode}_age") )
        tv_cv_height.text = String.format("HEIGHT: %s", getStringResourceByName("${mCharacterCode}_height") )
        tv_cv_weight.text = String.format("WEIGHT: %s", getStringResourceByName("${mCharacterCode}_weight") )
        tv_cv_hobbies.text = String.format("HOBBIES: %s", getStringResourceByName("${mCharacterCode}_hobbies") )
        tv_cv_birthday.text = String.format("BIRTHDAY: %s", getStringResourceByName("${mCharacterCode}_birthday") )

        //Generate Items
        val upArray : Array<Int> = Array(4, {i->0})
        val downArray : Array<Int> = Array(4, {i->0})
        val itemDocument = readXml()
        val itemList : NodeList = (itemDocument.getElementsByTagName("stat-items")
                .item(0) as Element).getElementsByTagName("item")
        for (i in 0..itemList.length - 1){
            var itemNode = itemList.item(i)
            if(itemNode.nodeType == Node.ELEMENT_NODE){
                val elem = itemNode as Element
                val itemID = elem.getAttribute("id") as String
                val itemName = elem.getElementsByTagName("name").item(0).textContent
                val itemType = elem.getElementsByTagName("type").item(0).textContent.toInt()
                val upNode = elem.getElementsByTagName("gain").item(0) as Element
                upArray[0] = upNode.getElementsByTagName("speed").item(0).textContent.toInt()
                upArray[1] = upNode.getElementsByTagName("might").item(0).textContent.toInt()
                upArray[2] = upNode.getElementsByTagName("sanity").item(0).textContent.toInt()
                upArray[3] = upNode.getElementsByTagName("knowledge").item(0).textContent.toInt()
                val downNode = elem.getElementsByTagName("lose").item(0) as Element
                downArray[0] = downNode.getElementsByTagName("speed").item(0).textContent.toInt()
                downArray[1] = downNode.getElementsByTagName("might").item(0).textContent.toInt()
                downArray[2] = downNode.getElementsByTagName("sanity").item(0).textContent.toInt()
                downArray[3] = downNode.getElementsByTagName("knowledge").item(0).textContent.toInt()
                val item : BetrayalStatItem = BetrayalStatItem(upArray, downArray, itemName, itemType)
                items.put(itemID, item)
            }
        }
    }

    private fun getStringResourceByName( name: String ) : String{
        val packageName : String = applicationContext.packageName
        val resID : Int = resources.getIdentifier(name, "string", packageName)
        return getString(resID)
    }

    private fun getImageResourceByName( name: String ) : Drawable {
        val packageName : String = applicationContext.packageName
        val resID : Int = resources.getIdentifier(name, "drawable", packageName)
        return getDrawable(resID)
    }

    private fun die(){
        img_character_view_portrait.setImageDrawable(getDrawable(R.drawable.default_portrait_dead))
        isDead = true
    }

    fun speedUp(view : View){
        if( currentSpeed < 7 && !isDead ) currentSpeed++
        tv_cv_speed.text = speedArray[currentSpeed].toString()
        img_cv_speed.setImageDrawable(getImageResourceByName("health_brtl_$currentSpeed"))
    }

    fun speedDown(view : View){
        if( currentSpeed > 0 && !isDead ){
            currentSpeed--
        } else {
            die()
        }
        tv_cv_speed.text = speedArray[currentSpeed].toString()
        img_cv_speed.setImageDrawable(getImageResourceByName("health_brtl_$currentSpeed"))
    }

    fun mightUp(view : View){
        if( currentMight < 7 && !isDead ) currentMight++
        tv_cv_might.text = mightArray[currentMight].toString()
        img_cv_might.setImageDrawable(getImageResourceByName("health_bltr_$currentMight"))
    }

    fun mightDown(view : View){
        if( currentMight > 0 && !isDead ){
            currentMight--
        } else {
            die()
        }
        tv_cv_might.text = mightArray[currentMight].toString()
        img_cv_might.setImageDrawable(getImageResourceByName("health_bltr_$currentMight"))
    }

    fun sanityUp(view : View){
        if( currentSanity < 7 && !isDead ) currentSanity++
        tv_cv_sanity.text = sanityArray[currentSanity].toString()
        img_cv_sanity.setImageDrawable(getImageResourceByName("health_brtl_$currentSanity"))
    }

    fun sanityDown(view : View){
        if( currentSanity > 0 && !isDead ){
            currentSanity--
        } else {
            die()
        }
        tv_cv_sanity.text = sanityArray[currentSanity].toString()
        img_cv_sanity.setImageDrawable(getImageResourceByName("health_brtl_$currentSanity"))
    }

    fun knowledgeUp(view : View){
        if( currentKnowledge < 7 && !isDead ) currentKnowledge++
        tv_cv_knowledge.text = knowledgeArray[currentKnowledge].toString()
        img_cv_knowledge.setImageDrawable(getImageResourceByName("health_bltr_$currentKnowledge"))
    }

    fun knowledgeDown(view : View){
        if( currentKnowledge > 0 && !isDead ){
            currentKnowledge--
        } else {
            die()
        }
        tv_cv_knowledge.text = knowledgeArray[currentKnowledge].toString()
        img_cv_knowledge.setImageDrawable(getImageResourceByName("health_bltr_$currentKnowledge"))
    }

    fun toggleAmulet(view: View){
        if( isDead ) return

        val item : BetrayalStatItem = items.get("amulet") ?: return
        val index = ITEM_IDS.AMULET.ordinal
        toggleItem(item, index)
    }

    fun toggleBell(view: View){
        if( isDead ) return

        val item : BetrayalStatItem = items.get("bell") ?: return
        val index = ITEM_IDS.BELL.ordinal
        toggleItem(item, index)
    }

    fun toggleBook(view: View){
        if( isDead ) return

        val item : BetrayalStatItem = items.get("book") ?: return
        val index = ITEM_IDS.BOOK.ordinal
        toggleItem(item, index)
    }

    fun toggleDog(view: View){
        if( isDead ) return

        val item : BetrayalStatItem = items.get("dog") ?: return
        val index = ITEM_IDS.DOG.ordinal
        toggleItem(item, index)
    }

    fun toggleGirl(view: View){
        if( isDead ) return

        val item : BetrayalStatItem = items.get("girl") ?: return
        val index = ITEM_IDS.GIRL.ordinal
        toggleItem(item, index)
    }

    fun toggleSymbol(view: View){
        if( isDead ) return

        val item : BetrayalStatItem = items.get("symbol") ?: return
        val index = ITEM_IDS.SYMBOL.ordinal
        toggleItem(item, index)
    }

    fun toggleLocket(view: View){
        if( isDead ) return

        val item : BetrayalStatItem = items.get("locket") ?: return
        val index = ITEM_IDS.LOCKET.ordinal
        toggleItem(item, index)
    }

    fun toggleMadman(view: View){
        if( isDead ) return

        val item : BetrayalStatItem = items.get("madman") ?: return
        val index = ITEM_IDS.MADMAN.ordinal
        toggleItem(item, index)
    }

    private fun toggleItem(item : BetrayalStatItem, index : Int){
        val builder = AlertDialog.Builder(this@CharacterActivity)
        builder.setTitle(item.name.toUpperCase())
        if (!inventory[index]){
            builder.setMessage("Pick up ${item.name}?\r\nSpeed: ${item.upSpeed}\r\n" +
                    "Might: ${item.upMight}\r\nSanity: ${item.upSanity}\r\nKnowledge: ${item.upKnowledge}")
            builder.setPositiveButton("YES"){dialog, which -> run{
                changeStats(item.upSpeed, item.upMight, item.upSanity, item.upKnowledge)
                inventory[index] = true}
            }
        } else {
            builder.setMessage("Drop ${item.name}?\r\nSpeed: ${item.downSpeed}\r\n" +
                    "Might: ${item.downMight}\r\nSanity: ${item.downSanity}\r\nKnowledge: ${item.downKnowledge}")
            builder.setPositiveButton("YES"){dialog, which -> run{
                changeStats(item.downSpeed, item.downMight, item.downSanity, item.downKnowledge)
                inventory[index] = false}
            }
        }
        builder.setNegativeButton("NO"){dialog, which -> null }

        val dialog: AlertDialog = builder.create()
        dialog.setOnShowListener( { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.characterColorBlack))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.characterColorBlack))})
        dialog.show()
        when( item.type.ordinal ){
            BetrayalStatItem.ITEMTYPE.ITEM.ordinal -> dialog.window.decorView.background.colorFilter = LightingColorFilter(colorMul, itemColor)
            BetrayalStatItem.ITEMTYPE.OMEN.ordinal -> dialog.window.decorView.background.colorFilter = LightingColorFilter(colorMul, omenColor)
        }

    }

    private fun changeStats(dSpeed : Int, dMight : Int, dSanity : Int, dKnowledge : Int){
        //Speed
        if (dSpeed > 0 ){
            var i = 0
            while( i < dSpeed ){
                speedUp(btn_cv_speed_up)
                i ++
            }
        } else if( dSpeed < 0 ){
            var i = 0
            while ( i > dSpeed ){
                speedDown(btn_cv_speed_down)
                i --
            }
        }
        //Might
        if (dMight > 0 ){
            var i = 0
            while( i < dMight ){
                mightUp(btn_cv_might_up)
                i ++
            }
        } else if( dMight < 0 ){
            var i = 0
            while ( i > dMight ){
                mightDown(btn_cv_might_down)
                i --
            }
        }
        //Sanity
        if (dSanity > 0 ){
            var i = 0
            while( i < dSanity ){
                sanityUp(btn_cv_sanity_up)
                i ++
            }
        } else if( dSanity < 0 ){
            var i = 0
            while ( i > dSanity ){
                sanityDown(btn_cv_sanity_down)
                i --
            }
        }
        //Knowledge
        if (dKnowledge > 0 ){
            var i = 0
            while( i < dKnowledge ){
                knowledgeUp(btn_cv_knowledge_up)
                i ++
            }
        } else if( dKnowledge < 0 ){
            var i = 0
            while ( i > dKnowledge ){
                knowledgeDown(btn_cv_knowledge_down)
                i --
            }
        }
    }

    fun resetCharacter(){
        currentSpeed = baseSpeed
        currentMight = baseMight
        currentSanity = baseSanity
        currentKnowledge = baseKnowledge
        isDead = false
        img_character_view_portrait.setImageDrawable(getDrawable(R.drawable.default_portrait))

        //Set stats:
        tv_cv_speed.text = speedArray[currentSpeed].toString()
        img_cv_speed.setImageDrawable(getImageResourceByName("health_brtl_$currentSpeed"))
        tv_cv_might.text = mightArray[currentMight].toString()
        img_cv_might.setImageDrawable(getImageResourceByName("health_bltr_$currentMight"))
        tv_cv_sanity.text = sanityArray[currentSanity].toString()
        img_cv_sanity.setImageDrawable(getImageResourceByName("health_brtl_$currentSanity"))
        tv_cv_knowledge.text = knowledgeArray[currentKnowledge].toString()
        img_cv_knowledge.setImageDrawable(getImageResourceByName("health_bltr_$currentKnowledge"))
    }

    fun manualReset(view : View){
        val builder = AlertDialog.Builder(this@CharacterActivity)
        builder.setTitle("Reset Character?")
        builder.setMessage("Are you sure you want to reset your stats?")
        builder.setPositiveButton("YES"){dialog, which -> resetCharacter() }
        builder.setNegativeButton("NO"){dialog, which -> null }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun readXml() : Document {
        //val xmlFile = File("res/xml/items.xml")
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val inStream = resources.openRawResource(R.raw.items)
        val xmlInput = InputSource(InputStreamReader(inStream))
       // val xmlInput = InputSource(StringReader(xmlFile.readText()))
        return dBuilder.parse(xmlInput)
    }
}
