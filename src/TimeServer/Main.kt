package TimeServer

import TimeServer.Handler.ServerHandler

fun main(args: Array<String>) {
    print("Choose a port: ")
    val port = readLine()!!.toInt()

    ServerHandler().runServer(port)
}