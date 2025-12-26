package gunGame.weapons

import enums.Items
import gunGame.dp
import internal.structure.StringFile

object LootTableGenerator {

    fun genLootTable(path:String, item: Items, name: String, lore: List<String>, nbt: String, customModelData: Int): String {
        val strOut = """{
            |  "type": "minecraft:command",
            |  "pools": [
            |    {
            |      "rolls": 1,
            |      "entries": [
            |        {
            |          "type": "minecraft:item",
            |          "name": "${item.value}",
            |          "functions": [
            |            {
            |              "function": "minecraft:set_custom_model_data",
            |              "value": $customModelData
            |            },
            |            {
            |              "function": "minecraft:set_lore",
            |              "lore": [${lore.joinToString(",")}],
            |              "mode": "insert"
            |            },
            |            {
            |              "function": "minecraft:set_name",
            |              "name":"$name",
            |              "entity": "this"
            |            },
            |            {
            |               "function": "minecraft:set_custom_data",
            |               "tag": "$nbt"
            |            }
            |          ]
            |        }
            |      ]
            |    }
            |  ]
            |}
""".trimMargin()
        val list = ArrayList<String>()
        list.addAll(strOut.split("\n"))
        val s = StringFile(path, list, "loot_table", "json")
        dp.addFile(s)
        return s.mcName
    }
}