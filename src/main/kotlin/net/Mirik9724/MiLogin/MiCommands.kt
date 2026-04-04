package net.Mirik9724.MiLogin

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.Mirik9724.MiLogin.MiLogin.Companion.authTimers
import net.Mirik9724.MiLogin.MiLogin.Companion.cache
import net.Mirik9724.MiLogin.MiLogin.Companion.data
import net.Mirik9724.MiLogin.MiLogin.Companion.isRegistered
import net.Mirik9724.MiLogin.MiLogin.Companion.log
import net.Mirik9724.MiLogin.MiLogin.Companion.notLoged
import net.Mirik9724.MiLogin.PasswordGuard.isSimple
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.time.LocalDateTime

fun cleanup(player: Player) {
    val name = player.username
    notLoged.remove(name)
    authTimers[name]?.cancel()
    authTimers.remove(name)
    MiLogin.bossBars[name]?.let {
        player.hideBossBar(it)
        MiLogin.bossBars.remove(name)
    }
}

class LogC(val ipc: String) {
    fun li(player: Player, args: Array<String>, server: ProxyServer) {
        if (MiLogin.isOnCooldown(player.username.toString())) {
            player.sendMessage(Component.text(data["wait3Sec"]!!).color(NamedTextColor.RED))
            return
        }
        MiLogin.updateCooldown(player.username.toString())

        if (args.size != 1) {
            return
        }

        val attempts = MiLogin.loginAttempts.getOrDefault(player.username.toString(), 0)
        if (attempts >= MiLogin.maxAttempts) {
            MiLogin.loginAttempts.remove(player.username.toString())
            player.disconnect(Component.text(data["tooMoreTrys"]!!).color(NamedTextColor.RED))
            return
        }

        if(Hash(args[0]) != cache[player.username.toString()]?.pass){
            player.sendMessage(Component.text(data["log.wrP"]!!).color(NamedTextColor.RED))

            val newAttempts = attempts + 1
            MiLogin.loginAttempts[player.username.toString()] = newAttempts
            return
        }

        player.sendMessage(Component.text(data["log.logged"]!!).color(NamedTextColor.GOLD))
        player.resetTitle()

        MiLogin.loginAttempts.remove(player.username.toString())
        cache[player.username.toString()]  = MiData(
            time = LocalDateTime.now().toString(),
            pass = cache[player.username.toString()]?.pass.toString(),
            ip = ipc
        )

        MiLogin.saveData()
        cleanup(player)

        MiLogin.factory.passLoginLimbo(player);
        log.info("player ${player.username.toString()} was logged")
    }
}

class RegC(val ipc: String) {
    fun li(player: Player, args: Array<String>) {
        if(isRegistered(player.username.toString())){return}
        if (MiLogin.isOnCooldown(player.username.toString())) {
            player.sendMessage(Component.text(data["wait3Sec"]!!).color(NamedTextColor.RED))
            return
        }
        MiLogin.updateCooldown(player.username.toString())

        if (args.size != 2) {
            return
        }

        if(args[0] != args[1]){
            player.sendMessage(Component.text(data["reg.difrPass"]!!).color(NamedTextColor.RED))
            return
        }

        if (isSimple(args[0], player.username.toString())) {
            return
        }

        player.sendMessage(Component.text(data["reg.registred"]!!).color(NamedTextColor.GOLD))
        player.resetTitle()

        cache[player.username.toString()]  = MiData(
            pass = Hash(args[0]),
            time = LocalDateTime.now().toString(),
            ip = ipc
        )
        MiLogin.saveData()
        cleanup(player)

        MiLogin.factory.passLoginLimbo(player);
        log.info("player ${player.username.toString()} was registered")
    }
}

class ChaC() : SimpleCommand {
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
            time = LocalDateTime.now().toString(),
            ip = source.getRemoteAddress().getAddress().getHostAddress().toString()
        )
        MiLogin.saveData()
    }
}

