package net.Mirik9724.MiLogin

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import net.Mirik9724.MiLogin.MiLogin.Companion.cache
import net.Mirik9724.MiLogin.MiLogin.Companion.data
import net.Mirik9724.MiLogin.MiLogin.Companion.isRegistered
import net.Mirik9724.MiLogin.MiLogin.Companion.lFactory
import net.Mirik9724.MiLogin.MiLogin.Companion.notLoged
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.time.LocalDateTime

class LogC {
    fun li(player: Player, args: Array<String>) {
        if (args.size != 1) {
            return
        }

        if(Hash(args[0]) != MiLogin.cache[player.username]?.pass){
            player.sendMessage(Component.text(data["log.wrP"]!!).color(NamedTextColor.RED))
        }

        player.sendMessage(Component.text(data["log.logged"]!!).color(NamedTextColor.GOLD))
        player.resetTitle()

        cache[player.username.toString()]  = MiData(
            time = LocalDateTime.now().toString()
        )
        MiLogin.saveData()


        notLoged.remove(player.username.toString())
        lFactory.respawnPlayer(player)
    }
}

class RegC {
    fun li(player: Player, args: Array<String>) {
        if(isRegistered(player.username.toString())){return}

        if (args.size != 2) {
            return
        }

        if(args[0] != args[1]){
            player.sendMessage(Component.text(data["reg.difrPass"]!!).color(NamedTextColor.RED))
            return
        }

        if (isSimple(args[0])) {
            return
        }

        player.sendMessage(Component.text(data["reg.registred"]!!).color(NamedTextColor.GOLD))
        player.resetTitle()

        cache[player.username.toString()]  = MiData(
            pass = Hash(args[0]),
            time = LocalDateTime.now().toString()
        )
        MiLogin.saveData()

        notLoged.remove(player.username.toString())
        lFactory.respawnPlayer(player)
    }
}

class ChaC : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        val args = invocation.arguments()

        if (source !is Player) {
            source.sendMessage(Component.text("Command only for player").color(NamedTextColor.RED))
            return
        }

        if (args.size != 2) {
            return
        }

        if(args[0] == args[1]) {
            source.sendMessage(Component.text(data["cha.onePass"]!!).color(NamedTextColor.RED))
            return
        }

        cache[source.username.toString()]  = MiData(
            pass = Hash(args[1]),
            time = LocalDateTime.now().toString()
        )
        MiLogin.saveData()
    }
}

