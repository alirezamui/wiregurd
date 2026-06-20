package com.example.data

data class WireGuardConfig(
    val privateKey: String,
    val address: String,
    val dns: String?,
    val mtu: Int?,
    val publicKey: String,
    val endpoint: String,
    val allowedIps: String,
    val persistentKeepalive: Int?
)

object VpnConfigParser {
    fun parse(content: String): WireGuardConfig {
        var privateKey: String? = null
        var address: String? = null
        var dns: String? = null
        var mtu: Int? = null
        var publicKey: String? = null
        var endpoint: String? = null
        var allowedIps: String? = null
        var persistentKeepalive: Int? = null

        var currentSection = ""

        val lines = content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith(";") }

        for (line in lines) {
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length - 1).trim().lowercase()
                continue
            }

            val parts = line.split("=", limit = 2)
            if (parts.size < 2) continue
            val key = parts[0].trim().lowercase()
            val value = parts[1].trim()

            when (currentSection) {
                "interface" -> {
                    when (key) {
                        "privatekey" -> privateKey = value
                        "address" -> address = value
                        "dns" -> dns = value
                        "mtu" -> mtu = value.toIntOrNull()
                    }
                }
                "peer" -> {
                    when (key) {
                        "publickey" -> publicKey = value
                        "endpoint" -> endpoint = value
                        "allowedips" -> allowedIps = value
                        "persistentkeepalive" -> persistentKeepalive = value.toIntOrNull()
                    }
                }
            }
        }

        // Validate fields as per user request
        if (privateKey == null) {
            throw IllegalArgumentException("Missing 'PrivateKey' in [Interface] section.")
        }
        if (privateKey.length != 44 && !privateKey.endsWith("=")) {
            throw IllegalArgumentException("Invalid 'PrivateKey' format. Must be a valid 32-byte Base64 key.")
        }
        if (address == null) {
            throw IllegalArgumentException("Missing 'Address' in [Interface] section.")
        }
        if (publicKey == null) {
            throw IllegalArgumentException("Missing 'PublicKey' in [Peer] section.")
        }
        if (publicKey.length != 44 && !publicKey.endsWith("=")) {
            throw IllegalArgumentException("Invalid 'PublicKey' format. Must be a valid 32-byte Base64 key.")
        }
        if (endpoint == null) {
            throw IllegalArgumentException("Missing 'Endpoint' in [Peer] section.")
        }
        
        val endpointParts = endpoint.split(":")
        if (endpointParts.size < 2 || endpointParts.last().toIntOrNull() == null) {
            throw IllegalArgumentException("Endpoint must be in 'host:port' format (e.g., 185.200.118.4:51820).")
        }

        return WireGuardConfig(
            privateKey = privateKey,
            address = address,
            dns = dns,
            mtu = mtu,
            publicKey = publicKey,
            endpoint = endpoint,
            allowedIps = allowedIps ?: "0.0.0.0/0",
            persistentKeepalive = persistentKeepalive
        )
    }
}
