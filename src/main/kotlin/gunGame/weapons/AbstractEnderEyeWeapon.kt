package gunGame.weapons

import abstractions.advancements.Advancement
import abstractions.advancements.UsingItem
import abstractions.flow.If
import abstractions.hasTag
import abstractions.score.Score
import commands.Command
import enums.Anchor
import enums.Items
import gunGame.playingTag
import gunGame.self
import lib.debug.Log
import structure.Fluorite
import structure.McFunction
import kotlin.math.abs
import kotlin.math.roundToInt

val directory = McFunction("jh1236:weapons/directory_ender") {
    Command.advancement().revoke(self).only("jh1236:rclick")
    AbstractEnderEyeWeapon.currentId.set(
        self.data["SelectedItem.components.minecraft:custom_data.weapon"].get()
    )
}
abstract class AbstractEnderEyeWeapon(
    name: String,
    damage: Int,
    customModelData: Int = 0,
    val cooldown: Double,
    val clipsize: Int = 1,
    val reload: Double = 0.0,
    secondary: Boolean = false,
    isReward: Boolean = false
) : AbstractWeapon(name, damage, secondary, isReward, customModelData) {
    companion object {
        val currentId: Score = Fluorite.reuseFakeScore("id")
    }

    open var damageStr = abs(damage).toString()
    open var cooldownStr = abs(cooldown).toString() + " seconds"
    open var reloadStr = abs(reload).toString() + " seconds"
    open var clipsizeStr = abs(clipsize).toString()
    val extraLines = arrayListOf<String>()

    val cdBeforeShot = Fluorite.reuseFakeScore("b4shot")

    val shootFunction: McFunction = McFunction(basePath)

    fun setup() {
        shootFunction += {
            shoot()
        }
        directory += {
            Command.execute().anchored(Anchor.EYES).If(self.hasTag(playingTag).canShoot()).run {
                If(currentId eq myId) {
                    shootFunction()
                }
            }
        }
        val lore = arrayListOf(
            """{"text" : "Damage: $damageStr", "color": "gray", "italic" : false}"""
        )
        if (clipsize > 1) {
            lore.add("""{"text":"Cooldown: $cooldownStr", "color": "gray", "italic" : false}""")
            lore.add("""{"text":"Clipsize: $clipsizeStr", "color": "gray", "italic" : false}""")
            lore.add("""{"text":"Reload Time: $reloadStr", "color": "gray", "italic" : false}""")
        } else {
            lore.add("{\"text\":\"Reload Time: $cooldownStr\", \"color\": \"gray\", \"italic\" : false}")
        }
        lore.addAll(extraLines)
        val sb = StringBuilder("""{ready:1b, weapon:$myId, cooldown: {value: 0, max : 0}, """)
        if (clipsize > 1) {
            sb.append("""ammo: {value: $clipsize, max:$clipsize}""")
        }
        sb.append("}")
        lootTable = LootTableGenerator.genLootTable(basePath, Items.ENDER_EYE, name, lore, sb.toString(), customModelData)
        setupInternal()
    }

    private fun shoot() {
        Log.debug("shot weapon with id $myId!!")
        If(!self["nbt = {SelectedItem:{components:{custom_data:{ready:0b}}}}"]) {
            cdBeforeShot.set(self.data["SelectedItem.components.minecraft:custom_data.cooldown.value"])
            if (clipsize != 1) {
                applyCoolDown(decrementClip((cooldown * 20).roundToInt(), (reload * 20).roundToInt()))
            } else if (cooldown > 0 || (cooldown * 20).roundToInt() == Score.LOWER) {
                applyCoolDown((cooldown * 20).roundToInt())
            }
        }
        fire()
    }

    abstract fun fire()
}

val a = Advancement("jh1236:rclick") {
    criteria {
        condition("requires") {
            trigger(UsingItem {
                item {
                    items {
                        item(Items.ENDER_EYE)
                    }
                }
            })
        }
    }
    rewards {
        function = directory
    }
}
