package gunGame.maps

import abstractions.Trigger
import abstractions.flow.If
import abstractions.flow.trees.ScoreTree
import abstractions.hasTag
import abstractions.schedule.Sleep
import abstractions.score.Objective
import commands.Command
import gunGame.respawnFunc
import gunGame.self
import gunGame.weapons.shootTag
import structure.McFunction
import utils.Duration
import utils.abs
import utils.get
import utils.rel

val tauntScore = Objective("taunt")
val tauntToPlay = Objective("tauntToHear")
val taunts = arrayListOf(
    "minecraft:entity.cat.ambient" to 1.0,
    "minecraft:block.honey_block.slide" to 0.0,
    "minecraft:entity.enderman.scream" to 1.0,
    "minecraft:entity.wither.spawn" to 2.0,
    "minecraft:item.goat_horn.sound.4" to 2.0,
    "minecraft:item.goat_horn.sound.5" to 1.0,
    "minecraft:entity.warden.tendril_clicks" to 0.0,
    "minecraft:block.amethyst_block.resonate" to 0.6,
    "minecraft:entity.ghast.scream" to 0.6,
    "minecraft:entity.allay.death" to 0.7,
    "minecraft:entity.witch.celebrate" to 1.0,
    "minecraft:block.conduit.attack.target" to 1.0,
    "minecraft:entity.warden.sniff" to 1.6,
    "minecraft:music_disc.otherside" to 1.0,
    "minecraft:entity.chicken.ambient" to 1.0
)

fun addShop() {
    Trigger("store") {
        If(!self["x = 7", "y = 2", " z = -33", "dx = 8", "dy = 6", "dz = 31"]) {
            respawnFunc()
            Command.tp(self, abs(11, 2, -18))
        }.Else {
            respawnFunc()
        }
    }
}

val playTaunt = McFunction("health/taunt") {
    tauntToPlay[self] = tauntScore['a'["limit=1"].hasTag(shootTag)]
    Sleep(Duration(20), keepContext = true)
    ScoreTree(tauntToPlay[self], 1..taunts.size) {
        val taunt = taunts[it - 1]
        Command.playsound(taunt.first).master(self, rel(), 100.0, taunt.second)
        if (it == 1 || it == 2 ||it == 7 || it == 8 || it == 12 || it == 13 || it == 15) {
            repeat(30) {
                Command.playsound(taunt.first).master(self, rel(), 100.0, taunt.second)
            }
        }
        if (it == 14) { //we cannot let the entirety of otherside play...

        Sleep(Duration.seconds(5))
        Command.stopsound(self)
        }
    }
}