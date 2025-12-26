package gunGame.weapons.impl

import abstractions.PlayerTag
import abstractions.flow.If
import abstractions.hasTag
import abstractions.notHasTag
import commands.Command
import enums.Effects
import enums.Particles
import gunGame.lagTag
import gunGame.self
import gunGame.weapons.*
import structure.Fluorite
import structure.McFunction
import utils.get
import utils.loc


class Minigun : RaycastEnderWeapon(
    "Minigun",
    200,
    16,
    -0.5,
    sound = listOf("block.note_block.hat" to 2.0),
    range = 400,
    spread = 2.0,
    clipSize = 60,
    reloadTime = 3.0,
    killMessage = """'["",{"selector": "@s","color": "gold"},{"text": " was mowed down by "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""",
    secondary = false,
) {

    companion object {
        var myId: Int = 0
        val minigunTag = PlayerTag("minigun2")
    }

    val fireFunc = McFunction("$basePath/shoot")


    init {
        showShooterParticle = false
        Minigun.myId = myId
        Fluorite.tickFile += {
            Command.execute()
                .asat(
                    'a'["nbt =! {SelectedItem:{components:{\"minecraft:custom_data\":{weapon:$myId}}}}"].hasTag(minigunTag)
                )
                .run {
                    Command.effect().clear(self, Effects.SLOWNESS)
                    resetAmmoForId(myId)
                    setCooldownForId(myId, (reload * 20).toInt())
                    minigunTag.remove(self)
                }
            Command.execute()
                .asat(
                    'a'["nbt = {SelectedItem:{component:s{minecraft:\"minecraft:custom_data\":{weapon:$myId}}}}"].notHasTag(
                        minigunTag
                    )
                ).unless(self["nbt = {SelectedItem:{components:{\"minecraft:custom_data\":{ammo:{value:$clipSize}}}}}"])
                .run {
                    Command.effect().clear(self, Effects.SLOWNESS)
                    resetAmmoForId(myId)
                    setCooldownForId(myId, (reload * 20).toInt())
                }
            minigunTag.remove('a'[""])

        }
        this.onRaycastTick = {
            Command.particle(Particles.CRIT, loc(), 0, 0, 0, 0, 1).force('a'[""].notHasTag(shootTag))
            Command.particle(Particles.CRIT, loc(0, 0, -0.0625), 0, 0, 0, 0, 1).force(
                'a'[""].notHasTag(shootTag).notHasTag(
                    lagTag
                )
            )
            Command.particle(Particles.CRIT, loc(0, 0, -0.125), 0, 0, 0, 0, 1).force(
                'a'[""].notHasTag(shootTag).notHasTag(
                    lagTag
                )
            )
            Command.particle(Particles.CRIT, loc(0, 0, -0.1875), 0, 0, 0, 0, 1).force(
                'a'[""].notHasTag(shootTag).notHasTag(
                    lagTag
                )
            )
        }
        this.onWallHit = {
            Command.particle(Particles.CRIT, loc(0, 0, -0.25), 0, 0, 0, 0, 1).force('a'[""].hasTag(shootTag))
        }
        setup()
    }

    override fun fire() {
        fireFunc.append {
            super.fire()
            minigunTag.add(self)
            Command.effect().give(self, Effects.SLOWNESS, 1, 10, true)
            If(decrementClip(0, 1) eq 1) {
                applyCoolDown((reload * 20).toInt())
                minigunTag.remove(self)
                Command.effect().clear(self, Effects.SLOWNESS)
            }
        }
        fireFunc()
    }
}
