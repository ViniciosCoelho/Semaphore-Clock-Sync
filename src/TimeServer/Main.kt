package TimeServer

import TimeServer.Handler.ServerHandler

fun main(args: Array<String>) {
    ServerHandler().runServer(4000)
}