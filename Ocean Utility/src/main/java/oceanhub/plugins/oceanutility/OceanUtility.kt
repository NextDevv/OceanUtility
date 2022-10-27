package oceanhub.plugins.oceanutility

import oceanhub.plugins.oceanutility.Iridium.IridiumColorAPI
import net.luckperms.api.LuckPerms
import oceanhub.plugins.oceanutility.Commands.OceanUtilityCommands
import oceanhub.plugins.oceanutility.Events.PlayerListenerEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import kotlin.properties.Delegates


@Suppress("DuplicatedCode")
class OceanUtility : JavaPlugin() {
    companion object Options{
        const val NAME = "Ocean Utility"
        const val VERSION = "1.0.0"

        const val PLUGIN_ID = "oceanhub.plugins.oceanutility"
        var PREFIX = ""
        var COLOR = ""
        var CHAT_FORMAT = ""
        var CHAT_FORMAT_NO_VIP = ""
        private var LuckPermsApi:LuckPerms by Delegates.notNull()
        private var ERROT_WHILE_TRYING_TO_LOAD_LUCKPERMS: Boolean = false
        var PLUGIN:OceanUtility by Delegates.notNull()

        fun getLuckPerms():LuckPerms? {
            if (ERROT_WHILE_TRYING_TO_LOAD_LUCKPERMS) return null
            return LuckPermsApi
        }

        fun getStaffGroupDisplayName(groupName: String): String {
            if(getLuckPerms() == null) return "null"
            val config = PLUGIN.config
            if(config.getBoolean("staff-groups.enabled")) {
                for(i in 1 until 100) {
                    val group = config.getString("staff-groups.staff-$i.name").toString()
                    println("$i. $group -> equal to $groupName = ${group == groupName}")
                    if(group == groupName) return config.getString("staff-groups.staff-$i.display-name").toString().tac()
                }
            }else return "null"
            return "null"
        }

        fun getStaffGroups(): MutableList<String> {
            if(getLuckPerms() == null) return mutableListOf()

            val config = PLUGIN.config
            val staffGroup = mutableListOf<String>()

            for(i in 0 until 100) {
                val group = config.getString("staff-groups.staff-$i.name")
                if(group!= null) staffGroup.add(group)
            }

            return staffGroup
        }

        fun getVipGroups(): MutableList<String> {
            if(getLuckPerms() == null) return mutableListOf()

            val config = PLUGIN.config
            val vipGroups = mutableListOf<String>()

            for(i in 0 until 100) {
                val group = config.getString("vips-groups.vip-$i.name")
                if(group!= null) vipGroups.add(group)
            }

            return vipGroups
        }

        fun hasChatColored(g: String): Boolean {
            if(getLuckPerms() == null) return false

            val config = PLUGIN.config

            for(i in 0 until 100) {
                val group = config.getString("vips-groups.vip-$i.name")
                if(group == g)
                    return config.getBoolean("vips-groups.vip-$i.chat-prefix-color-enabled")
            }

            for(i in 0 until 100) {
                val group = config.getString("staff-groups.staff-$i.name")
                if(group == g)
                    return config.getBoolean("staff-groups.staff-$i.chat-prefix-color-enabled")
            }

            return false
        }

        fun getChatPrefixName(g: String): String {
            if(getLuckPerms() == null) return g

            val config = PLUGIN.config

            for(i in 1 until 100) {
                val group = config.getString("staff-groups.staff-$i.name")
                println("$i. $group -> chat color: ${config.getString("staff-groups.staff-$i.chat-prefix-name").toString()}")
                if(group == g)
                    return config.getString("staff-groups.staff-$i.chat-prefix-name").toString()
            }

            for(i in 1 until 100) {
                val group = config.getString("vips-groups.vip-$i.name")
                if(group == g)
                    return config.getString("vips-groups.vip-$i.chat-prefix-name").toString()
            }

            return g
        }

        fun getChatColorPrefix(g: String): String {
            if(getLuckPerms() == null) return "&f".tac()

            val config = PLUGIN.config

            for(i in 1 until 100) {
                val group = config.getString("staff-groups.staff-$i.name")
                println("$i. $group -> chat color: ${config.getString("staff-groups.staff-$i.chat-prefix-color").toString()}")
                if(group == g)
                    return config.getString("staff-groups.staff-$i.chat-prefix-color").toString()
            }

            for(i in 1 until 100) {
                val group = config.getString("vips-groups.vip-$i.name")
                if(group == g)
                    return config.getString("vips-groups.vip-$i.chat-prefix-color").toString()
            }

            return "&f"
        }

        fun getVipGroupDisplayName(groupName: String): String {
            if(getLuckPerms() == null) return "null"
            val config = PLUGIN.config
            if(config.getBoolean("vips-groups.enabled")) {
                for(i in 1 until 100) {
                    val group = config.getString("vips-groups.vip-$i.name").toString()
                    if(group == groupName) return IridiumColorAPI.process(config.getString("vips-groups.vip-$i.display-name".tac()).toString())
                }
            }else return getLuckPerms()!!.groupManager.getGroup(groupName)!!.friendlyName ?: return "null"
            return "null"
        }
    }

    override fun onEnable() {
        // Plugin startup logic
        val console = server.consoleSender
        console.sendMessage(
            "&b  ___                     _   _ _   _ _ _ _        \n".tac() +
            "&b  / _ \\ __ ___ __ _ _ _   | | | | |_(_) (_) |_ _  _ \n".tac() +
            "&b | (_) / _/ -_) _` | ' \\  | |_| |  _| | | |  _| || |\n".tac() +
            "&b  \\___/\\__\\___\\__,_|_||_|  \\___/ \\__|_|_|_|\\__|\\_, |\n".tac() +
            "&b                                               |__/ ".tac()
        )
        console.sendMessage("Loading&b configuration&f file...".tac())

        saveDefaultConfig()
        PREFIX = config.getString("plugin-prefix-messages").toString().tac()
        COLOR = config.getString("plugin-prefix-color").toString().tac()
        if (config.getBoolean("luck-perms.chat-prefix.enabled"))
            CHAT_FORMAT = config.getString("luck-perms.chat-prefix.messages-prefix").toString().tac()
        PLUGIN = this
        if (config.getBoolean("luck-perms.chat-prefix.enabled"))
            CHAT_FORMAT_NO_VIP = config.getString("luck-perms.chat-prefix.messages-prefix-no-vip").toString().tac()

        console.sendMessage("Loading extra files...".tac())
        // Extra files

        console.sendMessage("Loading ocean utility data files...".tac())
        // Data files

        console.sendMessage("Loading ocean utility config files...".tac())
        // Config files

        console.sendMessage("Loading ocean utility commands...".tac())
        // Commands
        getCommand("oceanutility")?.setExecutor(OceanUtilityCommands)
        getCommand("oceanutility")?.tabCompleter = OceanUtilityCommands

        console.sendMessage("Loading ocean utility events modules...".tac())
        // Events modules
        Bukkit.getPluginManager().registerEvents(PlayerListenerEvent, this)

        console.sendMessage("Loading luck perms API modules...".tac())
        // Luck perms API modules
        try {
            val provider
            = Bukkit.getServicesManager().getRegistration(
                LuckPerms::class.java
            )

            if (provider != null)
                LuckPermsApi = provider.provider
            else
                throw NoClassDefFoundError("LuckPermsApi is not available")
        }catch (e:NoClassDefFoundError) {
            ERROT_WHILE_TRYING_TO_LOAD_LUCKPERMS = true
            console.sendMessage("&8[&cERROR&8]&f Failure to load LuckPerms API modules".tac())
            console.sendMessage("&8[&cERROR&8]&f Are you sure that luckperms is enabled?".tac())
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

fun String.tac(code: Char = '&'):String = ChatColor.translateAlternateColorCodes(code, this)