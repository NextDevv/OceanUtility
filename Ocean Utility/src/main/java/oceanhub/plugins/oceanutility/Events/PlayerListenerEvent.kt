@file:Suppress("DuplicatedCode", "KotlinConstantConditions")

package oceanhub.plugins.oceanutility.Events

import net.luckperms.api.model.group.Group
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import oceanhub.plugins.oceanutility.Iridium.IridiumColorAPI.process
import oceanhub.plugins.oceanutility.OceanUtility
import oceanhub.plugins.oceanutility.OceanUtility.Options.PLUGIN
import oceanhub.plugins.oceanutility.OceanUtility.Options.getLuckPerms
import oceanhub.plugins.oceanutility.Utility
import oceanhub.plugins.oceanutility.tac
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object PlayerListenerEvent:Listener {
    @EventHandler
    fun onPlayerChatEvent(e: AsyncPlayerChatEvent) {
        if(getLuckPerms() == null) {
            return
        }

        val lp = getLuckPerms() ?: return
        val player = e.player
        val user = lp.userManager.getUser(player.uniqueId) ?: return
        var group = lp.groupManager.getGroup(user.primaryGroup)!!.name

        if(group == "default") {
            group = lp.groupManager.getGroup(user.primaryGroup)!!.friendlyName ?: "default"
        }

        if(group == "null")
            group = PLUGIN.config.getString("luck-perms.chat-prefix.default-prefix").toString().tac()
        if(group == "default" || group == "user")
            group = PLUGIN.config.getString("luck-perms.chat-prefix.default-prefix").toString().tac()

        group = group.capitalize()

        println(group)

        var color = "#ffffff"

        if(OceanUtility.hasChatColored(player.getVip()))
            color = OceanUtility.getChatColorPrefix(player.getVip())
        if(OceanUtility.hasChatColored(lp.groupManager.getGroup(user.primaryGroup)!!.name ?: ""))
            color = OceanUtility.getChatColorPrefix(lp.groupManager.getGroup(user.primaryGroup)!!.name)

        println(color)

        color = color.tack()
        player.sendMessage(color)

        println(color)

        var textComponent = TextComponent(OceanUtility.CHAT_FORMAT
            .replace("{player}", player.name)
            .replace("{group}", color+group)
            .replace("{vip}".tac(), if(player.getVipName() == "Standard") "" else player.getVipName())
            .replace("{message}", color+e.message)
            .replace("%cp", ""))

        if(player.getVipName() == "Standard"){
            textComponent = TextComponent(OceanUtility.CHAT_FORMAT_NO_VIP
                .replace("{player}", player.name)
                .replace("{group}", color+group)
                .replace("{message}", color+e.message)
                .replace("%cp", "")
            )
        }

        e.format = textComponent.toPlainText()
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val lp = getLuckPerms() ?: return
        val player = e.player
        val user = lp.userManager.getUser(player.uniqueId) ?: return
        var group = OceanUtility.getStaffGroupDisplayName(lp.groupManager.getGroup(user.primaryGroup)!!.name) ?: return

        if(group == "default") {
            group = lp.groupManager.getGroup(user.primaryGroup)!!.friendlyName ?: "default"
        }

        if(group == "null")
            group = PLUGIN.config.getString("luck-perms.chat-prefix.default-prefix").toString().tac()
        if(group == "default")
            group = PLUGIN.config.getString("luck-perms.chat-prefix.default-prefix").toString().tac()

        for(vip in OceanUtility.getVipGroups()) {
            if(vip == user.primaryGroup) {
                group = OceanUtility.getVipGroupDisplayName(vip).tac()
            }
        }

        group = group.tack()

        e.player.setDisplayName("${group.tac()} &f${player.name}".tac())
        e.player.setPlayerListName("${group.tac()} &f${player.name}".tac())
        titleBar(e.player)
        scoreboard(e.player)
    }

    private fun scoreboard(player: Player) {
        val lp = getLuckPerms() ?: return
        val user = lp.userManager.getUser(player.uniqueId) ?: return
        var group = OceanUtility.getStaffGroupDisplayName(lp.groupManager.getGroup(user.primaryGroup)!!.name) ?: return

        if(group == "default") {
            group = lp.groupManager.getGroup(user.primaryGroup)!!.friendlyName ?: "default"
        }

        var isDefault = false

        if(group == "null")
            group = PLUGIN.config.getString("luck-perms.chat-prefix.default-prefix").toString().tac()
            isDefault = true
        if(group == "default")
        {
            group = PLUGIN.config.getString("luck-perms.chat-prefix.default-prefix").toString().tac()
            isDefault = true
        }

        for(vip in OceanUtility.getVipGroups()) {
            if(vip == user.primaryGroup) {
                group = OceanUtility.getVipGroupDisplayName(vip).tac()
            }
        }

        if(!isDefault) {
            group = group.tack()
        }

        val scoreboardManager = Bukkit.getScoreboardManager() ?: return
        val scoreboard = scoreboardManager.newScoreboard
        val objective = scoreboard.registerNewObjective("scoreboard", "dummy")

        val config = PLUGIN.config
        val titleAnimationSteps = config.getList("scoreboard.title-animation-steps") as MutableList<String>
        val titleAnimated = config.getBoolean("scoreboard.title-animated")
        val titleTick = config.getInt("scoreboard.animation-tick")
        val title = config.getString("scoreboard.title")

        objective.displaySlot = DisplaySlot.SIDEBAR

        if(titleAnimated) {
            object : BukkitRunnable() {
                var index = 0
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val current = LocalDateTime.now().format(formatter)

                override fun run() {
                    if(index > titleAnimationSteps.size-1)
                           index = 0
                    objective.displayName = titleAnimationSteps[index].tac()
                    index++

                    val body = config.getList("scoreboard.body") as MutableList<String>
                    body.reversed().forEachIndexed { index, it ->
                        val s = it
                            .replace("%p", player.name)
                            .replace("%r", group)
                            .replace("%date", current)
                            .replace("%online", Bukkit.getOnlinePlayers().size.toString())
                        val score = objective.getScore(s.tac())
                        score.score = index
                    }

                    player.scoreboard = scoreboard
                }
            }.runTaskTimer(PLUGIN, titleTick.toLong(), titleTick.toLong())
        }


    }

    private fun titleBar(player: Player) {

        val config = PLUGIN.config
        var titleCounter = 0
        var bodyContentTopIndex = 0
        var linkIndex1 = 0
        var linkIndex2 = 0
        var footerIndex = 0

        val titleAnimated = config.getBoolean("player-list-decorations.title.animated")
        val linksAnimated = config.getBoolean("player-list-decorations.body.links.animated")
        val footerAnimated = config.getBoolean("player-list-decorations.footer.animated")

        val title = config.getString("player-list-decorations.title.title") ?: ""

        val titleAnimatedList = config.getList("player-list-decorations.title.animation") as MutableList<String>

        val bodyContentTop = config.getList("player-list-decorations.body.content-top") as MutableList<String>

        val linkNonAnimatedList = config.getList("player-list-decorations.body.links.list-non-animated") as MutableList<String>

        val linkDiv1 = config.getString("player-list-decorations.body.links.list-animated.div-1")
        val linkAnimated1 = config.getList("player-list-decorations.body.links.list-animated.1") as MutableList<String>

        val linkDiv2 = config.getString("player-list-decorations.body.links.list-animated.div-2")
        val linkAnimated2 = config.getList("player-list-decorations.body.links.list-animated.2") as MutableList<String>

        val bodyContentFooter = config.getList("player-list-decorations.body.content-footer")

        val footer = config.getString("player-list-decorations.footer.footer")
        val footerAnimation = config.getList("player-list-decorations.footer.animation") as MutableList<String>

        var result = ""

        object : BukkitRunnable() {
            override fun run() {
                if(!player.isOnline)
                    this.cancel()

                if(titleCounter > titleAnimatedList.size-1)
                    titleCounter = 0

                result += if(titleAnimated) {
                    titleAnimatedList[titleCounter].tack()+"\n"
                }else title+"\n"
                titleCounter++

                result += config.getString("player-list-decorations.body.body-div".tac())+"\n"+"&f"



                bodyContentTop.map {
                    result += "${
                        process(it
                            .replace("%p", player.name)
                            .replace("%online", Bukkit.getOnlinePlayers().size.toString())
                            .replace("%om", Bukkit.getMaxPlayers().toString())
                        )
                    }\n"
                };result += "\n"

                // Titles
                if(linksAnimated) {
                    result += linkDiv1+"\n"
                    if(linkIndex1 > linkAnimated1.size-1)
                        linkIndex1 = 0
                    result += linkAnimated1[linkIndex1].tack().tac()+"\n"

                    result += linkDiv2+"\n"
                    if(linkIndex2 > linkAnimated2.size-1)
                        linkIndex2 = 0
                    result += linkAnimated2[linkIndex2].tack().tac()+"\n"

                    linkIndex1++
                    linkIndex2++
                }else {
                    linkNonAnimatedList.map { link ->
                        result += "${link}\n"
                    }
                }

                result += "\n"

                bodyContentFooter?.map {
                    result += "${it.toString().tack()}\n"
                }

                result += config.getString("player-list-decorations.body.body-div-footer")

                player.playerListHeader = result.tac()
                result = ""

                if (footer != null) {
                    if(footerAnimated) {
                        if(footerIndex > footerAnimation.size-1)
                            footerIndex = 0
                        result += footerAnimation[footerIndex]
                            .replace("%online", Bukkit
                                .getOnlinePlayers()
                                .size
                                .toString()
                            )
                            .replace("%om", Bukkit
                                .getMaxPlayers()
                                .toString()
                            )
                            .tac()+"\n"
                        footerIndex++
                    }else result += footer
                        .replace("%online", Bukkit
                            .getOnlinePlayers()
                            .size
                            .toString()
                        )
                        .replace("%om", Bukkit
                            .getMaxPlayers()
                            .toString()
                        )+"\n"
                }

                player.playerListFooter = result.tack().tac()
                result = ""
            }
        }.runTaskTimer(PLUGIN, 10, 10)

    }
}

private operator fun ChatColor.plus(s: String): String {
    return Utility.getStringWithColor(this, s)
}

fun String.prefix(prefix:String): String = "$prefix $this".tac()

fun Player.isInGroup(group: String): Boolean = hasPermission("group.$group")

fun String.tack():String {
    return if(this.contains('&')) ColorUtils.translateColorCodes(this)
    else ColorUtils.translateColorCodes("&$this")
}

fun Player.getVip(): String {
    if(getLuckPerms() == null) return ""
    val lp = getLuckPerms() ?: return ""

    val user = lp.userManager.getUser(uniqueId) ?: return ""
    val inheritedGroups: Collection<Group> = user.getInheritedGroups(user.queryOptions)
    for(i in OceanUtility.getVipGroups()) {
        val found = inheritedGroups.stream().anyMatch { g: Group ->
            g.name == i
        }
        if(found) {
            return i
        }
    }
    return ""
}

fun Player.getVipName(): String {
    if(getLuckPerms() == null) return ""
    val lp = getLuckPerms() ?: return ""

    val user = lp.userManager.getUser(uniqueId) ?: return ""
    val inheritedGroups: Collection<Group> = user.getInheritedGroups(user.queryOptions)
    for(i in OceanUtility.getVipGroups()) {
        val found = inheritedGroups.stream().anyMatch { g: Group ->
            g.name == i
        }
        return if(found) {
            OceanUtility.getVipGroupDisplayName(i)
        }else {
            "Standard"
        }
    }
    return ""
}

object ColorUtils {
    const val WITH_DELIMITER = "((?<=%1\$s)|(?=%1\$s))"

    /**
     * @param text The string of text to apply color/effects to
     * @return Returns a string of text with color/effects applied
     */
    fun translateColorCodes(text: String): String {
        val texts = text.split(String.format(WITH_DELIMITER, "&").toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val finalText = StringBuilder()
        var i = 0
        while (i < texts.size) {
            if (texts[i].equals("&", ignoreCase = true)) {
                //get the next string
                i++
                if (texts[i][0] == '#') {
                    finalText.append(ChatColor.of(texts[i].substring(0, 7)).toString() + texts[i].substring(7))
                } else {
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]))
                }
            } else {
                finalText.append(texts[i])
            }
            i++
        }
        return finalText.toString()
    }
}
