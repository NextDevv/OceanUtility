package oceanhub.plugins.oceanutility.Commands

import oceanhub.plugins.oceanutility.OceanUtility
import oceanhub.plugins.oceanutility.tac
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object OceanUtilityCommands : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = if(sender is Player) sender as Player else return true
        if(args.size > 1) {
            val sub = args[0]
            if(sub == "reload") {
                player.msg("Reloading configuration file...")
                OceanUtility.PLUGIN.reloadConfig()
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {

        var list: MutableList<String> = mutableListOf()
        if (args.size == 1)
            list = mutableListOf("oceanutility", "ou")
        if(command.name == "ou" || command.name == "oceanutility") {
            if(args.size == 2) {
                return mutableListOf("reload")
            }
        }

        return list
    }
}

private fun Player.msg(s: String) {
    sendMessage(s.tac())
}









