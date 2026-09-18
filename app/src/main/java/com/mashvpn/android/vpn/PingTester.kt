package com.mashvpn.android.vpn

import com.mashvpn.android.data.models.VpnNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.net.InetSocketAddress
import java.net.Socket

object PingTester {

    suspend fun testNodesPing(nodes: List<VpnNode>): List<VpnNode> = coroutineScope {
        nodes.map { node ->
            async(Dispatchers.IO) {
                val ping = measureTcpPing(node.remoteHost, node.remotePort)
                node.copy(pingMs = ping, isTestingPing = false)
            }
        }.awaitAll()
    }

    private fun measureTcpPing(host: String, port: Int, timeoutMs: Int = 2000): Long {
        return try {
            val startTime = System.currentTimeMillis()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
            }
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed > 0) elapsed else 1L
        } catch (e: Exception) {
            -1L
        }
    }
}
