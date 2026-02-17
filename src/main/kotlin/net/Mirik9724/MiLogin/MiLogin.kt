package net.Mirik9724.MiLogin;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import net.Mirik9724.api.logInit
import org.slf4j.Logger

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
class MiLogin @Inject constructor(val logger: Logger) {
    lateinit var log: Logger

    private fun initLimbo() {
        val loginWorld: VirtualWorld? = factory.createVirtualWorld(
            Dimension.THE_END,
            0.0, 64.0, 0.0,
            0f, 0f
        )

        val emptyBlock = factory.createSimpleBlock("minecraft:barrier")

        loginWorld?.setBlock(0, 63, 0, emptyBlock)
        loginWorld?.setBlock(0, 66, 0, emptyBlock)
        loginWorld?.setBlock(0, 64, 1, emptyBlock)
        loginWorld?.setBlock(1, 64, 0, emptyBlock)
        loginWorld?.setBlock(-1, 64, 0, emptyBlock)
        loginWorld?.setBlock(0, 64, -1, emptyBlock)

        nwFactory = factory.createLimbo(nickWorld).setName("loginWorld").setGameMode(GameMode.ADVENTURE)
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        log = logInit("MiLogin")

        initLimbo()
        logger.info("OK Limbo")

        log.info("Plugin ON")
    }
}
