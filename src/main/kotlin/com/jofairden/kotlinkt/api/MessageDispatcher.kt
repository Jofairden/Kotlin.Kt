package com.jofairden.kotlinkt.api

import com.jofairden.kotlinkt.model.OpCode
import com.jofairden.kotlinkt.util.JsonUtil
import mu.KotlinLogging

internal class MessageDispatcher(
    discordClient: DiscordClient
) {
    private val logger = KotlinLogging.logger { }
    private val guardian = discordClient.GatewayGuardian()

    fun dispatch(text: String) {
        val node = JsonUtil.Mapper.readTree(text)
        val opCode = OpCode.find(node["op"].asInt())

        if (opCode == null) {
            logger.error { "Impossible opCode: ${node["op"].asInt()}" }
            return
        }

        when (opCode) {
            OpCode.Dispatch -> guardian.dispatch(node)
            OpCode.Heartbeat -> guardian.heartbeat()
            OpCode.Identify -> opCode.warn()
            OpCode.StatusUpdate -> opCode.warn()
            OpCode.VoiceStateUpdate -> opCode.warn()
            OpCode.Resume -> opCode.warn()
            OpCode.Reconnect -> guardian.reconnect()
            OpCode.RequestGuildMembers -> opCode.warn()
            OpCode.InvalidSession -> {
                if (node["d"].asBoolean()) {
                    guardian.reconnect()
                } else {
                    // TODO Bot should terminate
                }
            }
            OpCode.Hello -> guardian.hello(node)
            OpCode.HeartbeatACK -> guardian.heartbeatAck()
        }
    }

    private fun OpCode.warn() {
        logger.warn { "Should not have received opcode ${this}: it should not be sent by Discord" }
    }
}
