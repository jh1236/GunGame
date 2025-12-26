package gunGame.weapons.impl

import FlameThrower
import abstractions.flow.If
import abstractions.hasTag
import abstractions.schedule.Sleep
import abstractions.score.Objective
import abstractions.score.Score
import commands.Command
import enums.*
import gunGame.self
import gunGame.weapons.*
import internal.commands.impl.Schedule
import lib.Delta
import lib.debug.Log
import structure.Fluorite
import structure.McFunction
import utils.Duration
import utils.Vec2
import utils.get
import utils.rel

lateinit var grapple: ProjectileWeapon
lateinit var carabine: RaycastWeapon
lateinit var antigrav: RaycastWeapon
lateinit var knockback: RaycastWeapon
lateinit var doubleBarreled: RaycastWeapon

fun loadGrapplingHook() {
    val strength = Objective("strength")
    val trueCooldown = 2.0
    grapple =
        ProjectileBuilder("Grappling Hook", 0).addParticle(Particles.DUST(0.0, 0.0, 0.0, 1.0)).withRange(50)
            .withProjectile(3.0).asSecondary()
            .withCooldown(9999.0).onWallHit {
                Command.execute().asat(it)
                    .facing('e'["limit = 1"].hasTag(ProjectileWeapon.currentProjectile), Anchor.FEET).rotated(
                        Vec2("~", "~-10")
                    ).run {
                        val f = McFunction {
                            If(ProjectileWeapon.hit eq 1) {
                                setCooldownForId(this@onWallHit.myId, (trueCooldown * 20).toInt())
                                Delta.launchFacing(strength['e'["limit = 1"].hasTag(ProjectileWeapon.currentProjectile)])
                            }.Else {
                                setCooldownForId(this@onWallHit.myId, 0)
                                Command.playsound("minecraft:block.note_block.didgeridoo").master(self)
                            }
                        }
                        val rotScore = Fluorite.reuseFakeScore("rotation")
                        Command.execute().summon(Entities.AREA_EFFECT_CLOUD).run {
                            Command.tp(self, rel(), Vec2("~", "~"))
                            rotScore.set(self.data["Rotation[1]"])
                        }
                        If(rotScore lt -30) {
                            Command.execute().rotated(Vec2("~", "-30")).run(f)
                        }.Else {
                            f()
                        }
                    }
            }.onProjectileSpawn {
                Command.execute().asat(it).run {
                    applyCoolDown(Score.LOWER)
                }
                strength[self] = 16000
                Log.info(strength[self])
            }.onProjectileTick {
                Command.playsound("minecraft:item.crossbow.loading_middle").master(it)
                strength[self] += 10
                this.cooldownStr = "$trueCooldown seconds"
            }.addSound("minecraft:entity.fishing_bobber.throw", .8)
            .addSound("minecraft:entity.fishing_bobber.retrieve", .5)
            .withCustomModelData(115).done()

}


fun loadCarbine() {
    carabine =
        RaycastBuilder("Carbine", 700).addParticle(Particles.CRIT).withReload(1.5).withCooldown(0.15).withClipSize(10)
            .addSound("minecraft:entity.camel.dash_ready", 1.0).withRange(200)
            .withKillMessage("""'["",{"selector": "@s","color": "gold"},{"text": " was filled with bullets by "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""")
            .withCustomModelData(15)
            .done()
}

fun loadAntiGrav() {
    antigrav = RaycastBuilder("Anti-Grav Gun", 0).asSecondary().withCooldown(0.4).addParticle(Particles.CHERRY_LEAVES)
        .addSound("minecraft:block.ancient_debris.hit", .25).addSound("minecraft:block.ancient_debris.hit", .25)
        .addSound("minecraft:block.ancient_debris.hit", .25).addSound("minecraft:block.ancient_debris.hit", .25)
        .addSound("minecraft:ui.loom.select_pattern", .75).withCustomModelData(116).withRange(300)
        .onEntityHit { hit, _ ->
            Command.effect().give(hit, Effects.LEVITATION, 3, 2, false)
            val temp = Fluorite.reuseFakeScore("temp")
            temp.set(self.data["SelectedItemSlot"])
            If(!(temp eq 8) and !(self.data["Inventory[{Slot:8b}]"])) {
                Command.item().replace.entity(hit, "hotbar.8").from.entity(hit, "weapon.mainhand")
                Command.item().replace.entity(hit, "weapon.mainhand").with(Items.AIR)
            }
            Sleep(Duration.seconds(3))
            Command.effect().give(hit, Effects.LEVITATION, 10, 255, false)
            Command.effect().give(hit, Effects.GLOWING, 10, 2, false)
        }
        .withKillMessage("""'["",{"selector": "@s","color": "gold"},{"text": " was floated away at the hands of "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""")
        .done()
}

fun loadKnockback() {
    knockback = RaycastBuilder("Knockback Gun", 100).withCooldown(
        0.0
    ).addParticle(Particles.ELECTRIC_SPARK)
        .addSound("minecraft:block.ancient_debris.hit", .25).addSound("minecraft:block.ancient_debris.hit", .25)
        .addSound("minecraft:block.ancient_debris.hit", .25).addSound("minecraft:block.ancient_debris.hit", .25)
        .addSound("minecraft:ui.loom.select_pattern", .75).withCustomModelData(17).withRange(300)
        .onEntityHit { hit, shoot ->
            Command.damage(hit, 1, "jh1236:shot").by(shoot)
        }
        .withKillMessage("""'["",{"selector": "@s","color": "gold"},{"text": " was pushed against a wall by "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""")
        .done()
}

fun loadDoubleBarreled() {

    doubleBarreled = RaycastBuilder("Double Barreled Shotgun", 500).addParticle(Particles.CRIT).withCooldown(2.5)
        .withClipSize(1).withSpread(10.0).withBulletsPerShot(16).addSound(Delta.EXPLODE_SOUND, 0.8)
        .addSound("block.chain.hit", 0.0).withCustomModelData(2).withRange(10).addSound("minecraft:block.iron_door.close", 0.0)
        .withKillMessage("""'["",{"selector": "@s","color": "gold"},{"text": " was blown to bits by "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""")
        .done()

}

fun loadExperiments() {
    FlameThrower()
    loadGrapplingHook()
    loadCarbine()
    Minigun()
    loadAntiGrav()
    loadKnockback()
    loadDoubleBarreled()
}
