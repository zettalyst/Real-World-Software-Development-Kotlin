package web_adapter

import TwootRepository
import Twootr
import database.DatabaseTwootRepository
import database.DatabaseUserRepository
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.IOException
import java.net.InetSocketAddress

class TwootrServer(address: InetSocketAddress) : WebSocketServer(address) {
    private val USER_NAME = "Joe"
    private val PASSWORD = "ahc5ez2aiV"
    private val OTHER_USER_NAME = "John"
    private val socketToEndPoint: MutableMap<WebSocket, WebSocketEndPoint> = HashMap()
    private val twootRepository: TwootRepository = DatabaseTwootRepository()
    private val twootr = Twootr(DatabaseUserRepository(), twootRepository)

    fun main(args: Array<String>) {
        val websocketAddress = InetSocketAddress("localhost", WEBSOCKET_PORT)
        val twootrServer = TwootrServer(websocketAddress)
        twootrServer.start()
        System.setProperty("org.eclipse.jetty.LEVEL", "INFO")
        val context = ServletContextHandler(ServletContextHandler.SESSIONS)
        context.setResourceBase(System.getProperty("user.dir") + "/src/main/webapp")
        context.setContextPath("/")
        val staticContentServlet = ServletHolder(
            "staticContentServlet", DefaultServlet::class.java
        )
        staticContentServlet.setInitParameter("dirAllowed", "true")
        context.addServlet(staticContentServlet, "/")
        val jettyServer = Server(STATIC_PORT)
        jettyServer.setHandler(context)
        jettyServer.start()
        jettyServer.dumpStdErr()
        jettyServer.join()
    }

    override fun onOpen(webSocket: WebSocket, handshake: ClientHandshake) {
        socketToEndPoint[webSocket] = WebSocketEndPoint(twootr, webSocket)
    }

    override fun onClose(webSocket: WebSocket, code: Int, reason: String, remote: Boolean) {
        socketToEndPoint.remove(webSocket);
    }

    override fun onMessage(webSocket: WebSocket, message: String?) {
        val endPoint = socketToEndPoint[webSocket]
        try {
            endPoint?.onMessage(message)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onError(webSocket: WebSocket, e: Exception) {
        e.printStackTrace()
    }

    override fun onStart() {
        // DO NOTHING
    }

    companion object {
        val STATIC_PORT = 8000
        val WEBSOCKET_PORT = 9000
    }

}