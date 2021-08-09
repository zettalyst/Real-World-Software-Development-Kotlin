package web_adapter

import Position
import ReceiverEndPoint
import SenderEndPoint
import Twoot
import Twootr
import com.fasterxml.jackson.databind.ObjectMapper
import org.java_websocket.WebSocket
import java.io.IOException

class WebSocketEndPoint(private val twootr: Twootr, private val webSocket: WebSocket) :
    ReceiverEndPoint {
    private var senderEndPoint: SenderEndPoint? = null

    @Throws(IOException::class)
    fun onMessage(message: String?) {
        val json = mapper.readTree(message)
        val cmd = json.get(CMD).asText()
        when (cmd) {
            "logon" -> {
                val userName = json.get("userName").asText()
                val password = json.get("password").asText()
                val endPoint = twootr.onLogon(userName, password, this)
                if (endPoint != null) {
                    senderEndPoint = null
                    webSocket.close()
                } else {
                    senderEndPoint = endPoint
                }
            }
            "follow" -> {
                val userName = json.get("userName").asText()
                sendStatusUpdate(senderEndPoint!!.onFollow(userName).toString())
            }
            "sendTwoot" -> {
                val id = json.get("id").asText()
                val content = json.get("content").asText()
                sendPosition(senderEndPoint!!.onSendTwoot(id, content))
            }
            "deleteTwoot" -> {
                val id = json.get("id").asText()
                sendStatusUpdate(senderEndPoint!!.onDeleteTwoot(id).toString())
            }
        }
    }

    override fun onTwoot(twoot: Twoot) {
        webSocket.send(
            java.lang.String.format(
                "{\"cmd\":\"twoot\", \"user\":\"%s\", \"msg\":\"%s\"}",
                twoot.senderId,
                twoot.content
            )
        )
    }

    private fun sendPosition(position: Position) {
        webSocket.send(
            java.lang.String.format(
                "{\"cmd\":\"sent\", \"position\":%s}",
                position.value
            )
        )
    }

    private fun sendStatusUpdate(status: String) {
        webSocket.send(
            String.format(
                "{\"cmd\":\"statusUpdate\", \"status\":\"%s\"}",
                status
            )
        )
    }

    companion object {
        private val mapper = ObjectMapper()
        private const val CMD = "cmd"
    }
}
