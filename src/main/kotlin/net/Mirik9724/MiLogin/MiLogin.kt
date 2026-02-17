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
    authors = ["Mirik9724"]
)
class MiLogin @Inject constructor(val logger: Logger) {
    lateinit var log: Logger

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        log = logInit("MiLogin")

        log.info("Plugin ON")
    }
}
