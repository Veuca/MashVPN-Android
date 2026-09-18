package com.mashvpn.android.vpn

data class ParsedOvpnConfig(
    val remoteHost: String,
    val remotePort: Int,
    val isTcp: Boolean,
    val dnsServers: List<String> = listOf("1.1.1.1", "8.8.8.8"),
    val mtu: Int = 1500
)

object OpenVpnParser {

    fun parse(rawConfig: String, defaultHost: String, defaultPort: Int, defaultProto: String): ParsedOvpnConfig {
        var host = defaultHost
        var port = defaultPort
        var isTcp = defaultProto.equals("tcp", ignoreCase = true)

        val lines = rawConfig.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("remote ", ignoreCase = true)) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 2 && parts[1].isNotBlank()) {
                    host = parts[1]
                }
                if (parts.size >= 3) {
                    parts[2].toIntOrNull()?.let { port = it }
                }
            } else if (trimmed.startsWith("proto ", ignoreCase = true)) {
                if (trimmed.contains("tcp", ignoreCase = true)) {
                    isTcp = true
                }
            }
        }

        return ParsedOvpnConfig(
            remoteHost = host,
            remotePort = port,
            isTcp = isTcp
        )
    }
}
