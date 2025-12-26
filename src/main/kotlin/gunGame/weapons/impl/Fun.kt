package gunGame.weapons.impl

import abstractions.PlayerTag
import abstractions.Trigger
import abstractions.flow.If
import abstractions.flow.Switch
import abstractions.flow.trees.ScoreTree
import abstractions.hasTag
import abstractions.notHasTag
import abstractions.schedule.Sleep
import abstractions.score.Objective
import abstractions.score.Score
import abstractions.variables.NBTTypes
import commands.Command
import enums.Anchor
import enums.Entities
import enums.Items
import enums.Particles
import gunGame.health
import gunGame.self
import gunGame.streak
import gunGame.weapons.*
import lib.Delta
import lib.debug.Log
import lib.random.Random
import structure.McFunction
import utils.Duration
import utils.Vec2
import utils.get
import utils.rel

lateinit var weezer: RaycastWeapon
lateinit var shuffler: RaycastWeapon
lateinit var danceGun: RaycastWeapon
lateinit var musicalNuke: ProjectileWeapon
val handleStreak = McFunction("jh1236:health/streak")
val song = Objective("song")

val songs = listOf(
    "custom.gangnum" to 4 * 60 + 8,
    "custom.breakeven" to 262,
    "custom.fallen_kingdom" to 4 * 60 + 10,
    "custom.groove" to 5 * 60 + 40,
    "custom.iris" to 4 * 60 + 50,
    "custom.levels" to 1 * 60 + 32,
    "custom.payphone" to 3 * 60 + 52,
    "custom.pompeii" to 3 * 60 + 35,
    "custom.rickroll" to 3 * 60 + 32,
    "custom.save_a_life" to 4 * 60 + 42,
    "custom.unholy" to 2 * 60 + 37,
    "custom.50_ways" to 4 * 60 + 8,
    "custom.dont_you_worry" to 3 * 60 + 33,
    "custom.waiting_for_love" to 3 * 60 + 51,
    "custom.church" to 4 * 60 + 16,
)


fun loadDance() {
    Trigger("jukebox") {
        If(it eq 1) {
            Command.tell(self, """{"text":"Songs:", "underline" : true}""")
            for ((i, v) in songs.withIndex()) {
                Command.tellraw(self, """${i + 2}: ${v.first.split(".")[1]}""")
            }
            Command.tellraw(self, """${songs.size + 2}: Random""")
        }.Else {
            song[self] = it
        }
    }


    danceGun = RaycastBuilder("Dance Gun", 500).withCooldown(1.0).withCustomModelData(112)
        .withKillMessage("""'["",{"selector": "@s","color": "gold"},{"text": " lost a dance-off with "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""")
        .withPiercing()
        .withRange(100)
        .addSound("minecraft:block.beacon.power_select", 2.0)
        .addParticle(Particles.DUST(3.0, 3.0, 3.0, 2.0), 4)
        .onWallHit {
            Command.raw("""summon item ~ ~ ~ {Age:5950,PickupDelay:32767,Item:{id:"minecraft:music_disc_13",Count:1b}, Tags:[new, safe]}""")
            Command.raw("""summon item ~ ~ ~ {Age:5950,PickupDelay:32767,Item:{id:"minecraft:music_disc_mellohi",Count:1b}, Tags:[new, safe]}""")
            Command.raw("""summon item ~ ~ ~ {Age:5950,PickupDelay:32767,Item:{id:"minecraft:music_disc_blocks",Count:1b}, Tags:[new, safe]}""")
            Command.raw("""summon item ~ ~ ~ {Age:5950,PickupDelay:32767,Item:{id:"minecraft:music_disc_cat",Count:1b}, Tags:[new, safe]}""")
            Command.raw("""summon item ~ ~ ~ {Age:5950,PickupDelay:32767,Item:{id:"minecraft:music_disc_otherside",Count:1b}, Tags:[new, safe]}""")
            Command.raw("""summon item ~ ~ ~ {Age:5950,PickupDelay:32767,Item:{id:"minecraft:music_disc_far",Count:1b}, Tags:[new, safe]}""")
            Command.raw("""summon item ~ ~ ~ {Age:5950,PickupDelay:32767,Item:{id:"minecraft:music_disc_chirp",Count:1b}, Tags:[new, safe]}""")
            Command.execute().asat('e'["tag = new"]).run {
                repeat(3) {
                    val score = Random.next(40)
                    score -= 20
                    self.data["Motion[$it]", NBTTypes.DOUBLE, .02] = score
                }
            }
        }
        .onEntityHit { hit, shoot ->
            If(!(song[shoot] eq song[shoot])) {
                song[shoot] = 2
            }
            Command.execute().facing(shoot, Anchor.EYES).run.tp(hit, rel(), Vec2("~180", "~"))
            val value = song[shoot].copy()
            If(song[shoot] eq songs.size + 2) {
                value.set { Command.random().value(2..songs.size + 2) }
            }
            ScoreTree(value, 2 until songs.size + 2) {
                val sound = songs[it - 2].first
                Log.info("playin $sound")
                val len = songs[it - 2].second
                Command.playsound("minecraft:$sound").master(self, rel(), 100.0)
                //TODO: fix!
//                        Command.raw("execute at @s run particle minecraft:entity_effect ~ ~ ~ 0.9960784313725490196078431372549 0.9921568627450980392156862745098 0.0039215686274509803921568627451 1 0 force @s")
//                        Sleep(Duration.seconds(len))
//                        Command.raw("particle minecraft:entity_effect ~ ~ ~ 0.9960784313725490196078431372549 0.9921568627450980392156862745098 1 1 0 force @s")
            }
        }.asReward().done()
    danceGun.extraLines.add("""{"text":"Forces the user to listen to music on hit.","color" : "gray","italic":false}""")
    danceGun.extraLines.add("""{"text":"(Select the song using /trigger jukebox).","color" : "gray","italic":false}""")
}

fun loadWeezerGun() {
    weezer = RaycastBuilder("Weezer Gun", 500).withCooldown(1.0).withCustomModelData(114)
        .withKillMessage("""'["",{"selector": "@s","color": "gold"},{"text": " was Weezed by "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""")
        .withPiercing()
        .withRange(100)
        .addSound("minecraft:block.note_block.guitar", 1.4)
        .addParticle(Particles.DUST(.3, .1, 3.0, 1.3), 4)
        .onEntityHit { hit, _ ->
            Command.playsound("custom.weezer").master(hit, rel(), 1.0)
            Sleep(Duration(41))
            Command.item().replace.entity(self, "armor.head")
                .with(Items.CARVED_PUMPKIN.enchantments("binding_curse" to 1))
        }.asReward().done()
    weezer.extraLines.add("""{"text":"Weezes the victim.","color" : "gray","italic":false}""")
}

fun loadInventoryGun() {
    shuffler = RaycastBuilder("Inventory Gun", 500).withCooldown(1.0).withCustomModelData(117)
        .withKillMessage("""'["",{"selector": "@s","color": "gold"},{"text": " was confused by "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""")
        .withPiercing()
        .withRange(100)
        .addSound("entity.allay.item_taken", 1.4)
        .addParticle(Particles.SWEEP_ATTACK)
        .onEntityHit { hit, _ ->
            val rand = java.util.Random(1)
            repeat(9) {
                If(self.data["Inventory[{Slot:${it}b}]"]) {
                    Command.item().replace.entity(hit, "inventory.${rand.nextInt(9)}").from.entity(hit, "hotbar.$it")
                    Command.item().replace.entity(hit, "hotbar.$it").with(Items.AIR)
                }
            }
        }.asReward().done()
    weezer.extraLines.add("""{"text":"Shuffles the victims inventory.","color" : "gray","italic":false}""")
}

fun loadMusicalNuke() {
    val bang = PlayerTag("bang")
    val nuke = PlayerTag("nuke")
    musicalNuke = ProjectileBuilder("Musical Nuke", 500).withProjectile(5.0).withCooldown(1.0).withCustomModelData(114)
        .withKillMessage("""'["",{"selector": "@s","color": "gold"},{"text": " was shattered by "},{"selector": "@a[tag=$shootTag]","color": "gold"}]'""")
        .withPiercing()
        .withRange(100)
        .addSound("entity.allay.item_taken", 1.4)
        .addParticle(Particles.DUST(1.0, 0.0, 0.0, 0.7))
        .onWallHit { shoot ->
            If(self.notHasTag(bang)) {
                shotCount[self] = 100 //this is cheeky I know
                Command.summon(Entities.TNT, rel(0, 1, 0), "{Glowing:1b,Fuse:101}")
            }
            health[self] = shotCount[self]
            bang.add(self)
        }.onProjectileSpawn {
            val shoot = 'a'["limit = 1"].hasTag(shootTag)
            Command.clear(shoot, Items.CARROT_ON_A_STICK.customData("weapon" to myId), 1)
        }
        .onProjectileTick {
            shotCount[self] -= 1
            If(shotCount[self] eq 1 and self.hasTag(bang)) {
                health[self] = 0
                Command.kill('e'["type=tnt", "distance = ..1"])
                val shoot = 'a'["limit = 1"].hasTag(shootTag)
                If(!(song[shoot] eq song[shoot])) {
                    song[shoot] = 2
                }
                Command.playsound(Delta.EXPLODE_SOUND).master('a'[""], rel(), 3, 1)
                Command.particle(Particles.DUST(3.0, 3.0, 3.0, 3.0), rel(), 7.5, 7.5, 7.5, 2, 1000).force('a'[""])
                Command.execute().asat('a'["distance=..30"]).run {
                    val value = song[shoot].copy()
                    If(song[shoot] eq songs.size + 2) {
                        value.set { Command.random().value(2..songs.size + 2) }
                    }
                    ScoreTree(value, 2 until songs.size + 2) {
                        val sound = songs[it - 2].first
                        Log.info("playin $sound")
                        val len = songs[it - 2].second
                        Command.playsound("minecraft:$sound").master(self, rel(), 100.0)
                        //TODO: fix!
//                        Command.raw("execute at @s run particle minecraft:entity_effect ~ ~ ~ 0.9960784313725490196078431372549 0.9921568627450980392156862745098 0.0039215686274509803921568627451 1 0 force @s")
//                        Sleep(Duration.seconds(len))
//                        Command.raw("particle minecraft:entity_effect ~ ~ ~ 0.9960784313725490196078431372549 0.9921568627450980392156862745098 1 1 0 force @s")
                    }
                }
            }
        }.asReward().done()
    musicalNuke.extraLines.add("""{"text":"Uh oh.","color" : "gold","italic":false}""")
}

fun loadStreakRewards() {
    Switch(streak[self], allowDuplicateMatches = true)
        .case(3) {
            Command.tellraw(
                'a'[""],
                """["",{"selector":"@s", "color":"gold"}," is on ",{"text":"Fire!","color" : "red"}]"""
            )
            danceGun.give(self)

        }
        .case(5) {
            Command.tellraw(
                'a'[""],
                """["", {"selector":"@s","color": "gold"}, " is ", {"text": "UNSTOPPABLE!","bold": true,"color":"red"}]"""
            )
            weezer.give(self)
        }
        .case(7) {
            Command.tellraw(
                'a'[""],
                """["", {"selector":"@s","color": "gold"}, " is ", {"text": "INSANE!","bold": true,"color":"blue", "underlined":true}]"""
            )
            shuffler.give(self)
        }
        .case(10) {
            Command.tellraw(
                'a'[""],
                """["", {"selector":"@s","color": "gold"}, " is ", {"text":"aa","obfuscated": true, "color": "dark_purple"}, {"text": "BLOODTHIRSTY!","bold": true,"color":"dark_red"} ,{"text":"aa","obfuscated": true, "color": "dark_purple"}]"""
            )
            musicalNuke.give(self)
        }.case(11..Score.UPPER) {
            musicalNuke.give(self)
        }
}

fun loadFun() {
    loadDance()
    loadWeezerGun()
    loadInventoryGun()
    loadMusicalNuke()
    handleStreak.append { loadStreakRewards() }
}
