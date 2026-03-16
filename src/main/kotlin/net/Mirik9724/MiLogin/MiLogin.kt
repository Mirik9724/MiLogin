package net.Mirik9724.MiLogin;

import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.google.inject.Inject
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.Mirik9724.api.copyFileFromJar
import net.Mirik9724.api.loadYmlFile
import net.elytrium.limboapi.api.LimboSessionHandler
import net.Mirik9724.api.logInit
import net.Mirik9724.api.tryCreatePath
import net.Mirik9724.api.updateYmlFromJar
import net.elytrium.limboapi.api.Limbo
import net.elytrium.limboapi.api.LimboFactory
import net.elytrium.limboapi.api.chunk.Dimension
import net.elytrium.limboapi.api.chunk.VirtualWorld
import net.elytrium.limboapi.api.command.LimboCommandMeta
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent
import net.elytrium.limboapi.api.player.GameMode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.slf4j.Logger
import java.io.File
import java.time.Duration


@Plugin(
    id = "milogin",
    name = "MiLogin",
    version = BuildConstants.VERSION,
    description = "s",
    url = "https://github.com/Mirik9724/MiLogin",
    authors = ["Mirik9724"],
    dependencies = [
        Dependency(id = "limboapi"),
        Dependency(id = "mirikapi"),
        Dependency(id = "whitelist-ultra", optional = true)
    ]
)
class MiLogin @Inject constructor(private val server: ProxyServer) {
    lateinit var log: Logger
    lateinit var factory: LimboFactory

    companion object {
        internal lateinit var salt: ByteArray
        internal var data = mapOf<String, String>()
        lateinit var lFactory: Limbo
        val notLoged = mutableListOf<String>()
        val pth = "plugins/MiLogin/"
        val dtFile = File(pth+"data.json")


        val sltFl = File(pth + "salt.txt")
        val conf = "config.yml"

        lateinit var cache: MutableMap<String, MiData>
        private val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()



        fun setupData() {
            if (!dtFile.exists()) {
                dtFile.parentFile.mkdirs()
                cache = mutableMapOf()
                saveData()
            } else {
                val type = object : com.google.gson.reflect.TypeToken<MutableMap<String, MiData>>() {}.type
                cache = dtFile.reader().use { gson.fromJson(it, type) } ?: mutableMapOf()
            }
        }

        fun saveData() {
            val root = if (dtFile.exists() && dtFile.length() > 0) {
                gson.fromJson(dtFile.readText(), JsonObject::class.java)
            } else {
                JsonObject()
            }

            cache.forEach { (nick, data) ->
                val playerJson = root.getAsJsonObject(nick) ?: JsonObject()

                data.pass.takeIf { it.isNotEmpty() }?.let { playerJson.addProperty("pass", it) }
                data.time.takeIf { it.isNotEmpty() }?.let { playerJson.addProperty("time", it) }

                root.add(nick, playerJson)
            }

            dtFile.writeText(gson.toJson(root))
        }



        fun isRegistered(nick: String): Boolean = nick in cache
    }

    private fun initLimbo() {
        val loginWorld: VirtualWorld? = factory.createVirtualWorld(
            Dimension.THE_END,
            0.0, 64.0, 0.0,
            0f, 0f
        )

        val block = factory.createSimpleBlock("minecraft:barrier")

        loginWorld!!.setBlock(0, 63, 0, block)

        loginWorld!!.setBlock(0, 64, 1, block)
        loginWorld!!.setBlock(1, 64, 0, block)
        loginWorld!!.setBlock(-1, 64, 0, block)
        loginWorld!!.setBlock(0, 64, -1, block)
        loginWorld!!.setBlock(1, 64, -1, block)
        loginWorld!!.setBlock(1, 64, 1, block)
        loginWorld!!.setBlock(-1, 64, 1, block)
        loginWorld!!.setBlock(-1, 64, -1, block)

        loginWorld!!.setBlock(0, 65, 1, block)
        loginWorld!!.setBlock(1, 65, 0, block)
        loginWorld!!.setBlock(-1, 65, 0, block)
        loginWorld!!.setBlock(0, 65, -1, block)
        loginWorld!!.setBlock(1, 65, -1, block)
        loginWorld!!.setBlock(1, 65, 1, block)
        loginWorld!!.setBlock(-1, 65, 1, block)
        loginWorld!!.setBlock(-1, 65, -1, block)

        loginWorld!!.setBlock(0, 66, 0, block)

        val loginMeta = LimboCommandMeta(listOf("login", "l", "log"))
        val regMeta = LimboCommandMeta(listOf("register", "reg", "r"))
        val changeMeta = LimboCommandMeta(listOf("change"))

        lFactory = factory.createLimbo(loginWorld).setName("loginWorld").setGameMode(GameMode.ADVENTURE)
            .registerCommand(loginMeta)
            .registerCommand(regMeta)
            .registerCommand(changeMeta)
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        log = logInit("MiLogin")

        factory = server.pluginManager
            .getPlugin("limboapi")
            .flatMap { it.getInstance() }
            .orElseThrow() as LimboFactory
        initLimbo()
        log.info("OK Limbo")

        tryCreatePath(File(pth))

        copyFileFromJar(conf,pth, this::class.java.classLoader)
        updateYmlFromJar(conf, pth + conf, this::class.java.classLoader)

        data = loadYmlFile(pth+conf)

        if (sltFl.exists() && sltFl.length() > 0) {
            salt = sltFl.readBytes()
        } else {
            salt = generateSalt()
            sltFl.writeBytes(salt)
        }

        server.commandManager.register(server.commandManager.metaBuilder("change").plugin(this).build(),
            ChaC())


        setupData()

        log.info("Plugin ON")
    }

    @Subscribe
    fun onLoginLimboRegister(event: LoginLimboRegisterEvent) {
        event.addOnJoinCallback {
            val player = event.player
            notLoged.add(event.player.username.toString())

            val sessionHandler = object : LimboSessionHandler {
                override fun onChat(chat: String) {

                    if (chat.startsWith("/")) {
                        val full = chat.removePrefix("/")
                        val parts = full.split(" ")
                        val cmd = parts[0].lowercase()
                        val args = parts.drop(1).toTypedArray()

                        when (cmd) {
                            "login", "l", "log" -> LogC().li(player, args)
                            "register", "reg", "r" -> RegC().li(player, args)
//                            "change" -> ChaC().execute(player, args)
                        }
                    } else {
                    }
                }
            }

            lFactory.spawnPlayer(player, sessionHandler)


            if(!isRegistered(player.username.toString())) {
                player.showTitle(Title.title(
                Component.text(data["reg.main"]!!),
                Component.text(data["reg.litl"]!!),
                Title.Times.times(
                    Duration.ofMillis(500), Duration.ofHours(1000), Duration.ofMillis(500)
                )
                ))
            }
            else{
                player.showTitle(Title.title(
                    Component.text(data["log.main"]!!),
                    Component.text(data["log.litl"]!!),
                    Title.Times.times(
                        Duration.ofMillis(500), Duration.ofHours(1000), Duration.ofMillis(500)
                    )
                ))
            }
        }
    }
}
