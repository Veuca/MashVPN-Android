package com.mashvpn.android.vpn

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.mashvpn.android.MainActivity
import com.mashvpn.android.MashVpnApp
import com.mashvpn.android.R
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel

class MashVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.mashvpn.android.CONNECT"
        const val ACTION_DISCONNECT = "com.mashvpn.android.DISCONNECT"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_RAW_CONFIG = "extra_raw_config"
        const val EXTRA_HOST = "extra_host"
        const val EXTRA_PORT = "extra_port"
        const val EXTRA_PROTO = "extra_proto"

        private const val NOTIFICATION_ID = 1001

        var currentState: VpnState = VpnState.Disconnected
            private set

        private val stateListeners = mutableListOf<(VpnState) -> Unit>()

        fun addStateListener(listener: (VpnState) -> Unit) {
            stateListeners.add(listener)
            listener(currentState)
        }

        fun removeStateListener(listener: (VpnState) -> Unit) {
            stateListeners.remove(listener)
        }

        private fun updateState(newState: VpnState) {
            currentState = newState
            stateListeners.forEach { it(newState) }
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var currentServerName = "مش وی پی ان"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val serverName = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "سرور مش وی پی ان"
                val rawConfig = intent.getStringExtra(EXTRA_RAW_CONFIG) ?: ""
                val host = intent.getStringExtra(EXTRA_HOST) ?: "127.0.0.1"
                val port = intent.getIntExtra(EXTRA_PORT, 1194)
                val proto = intent.getStringExtra(EXTRA_PROTO) ?: "udp"

                currentServerName = serverName
                startVpnTunnel(serverName, rawConfig, host, port, proto)
            }
            ACTION_DISCONNECT -> {
                stopVpnTunnel()
            }
        }
        return START_NOT_STICKY
    }

    private fun startVpnTunnel(
        serverName: String,
        rawConfig: String,
        host: String,
        port: Int,
        proto: String
    ) {
        updateState(VpnState.Connecting(serverName))
        startForeground(NOTIFICATION_ID, buildNotification("در حال برقراری اتصال ایمن...", serverName))

        serviceScope.launch {
            try {
                // Parse configuration
                val config = OpenVpnParser.parse(rawConfig, host, port, proto)

                // Build Vpn Interface
                val builder = Builder()
                    .setSession(serverName)
                    .setMtu(config.mtu)
                    .addAddress("10.8.0.2", 24)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("1.1.1.1")
                    .addDnsServer("8.8.8.8")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    builder.setMetered(false)
                }

                vpnInterface?.close()
                vpnInterface = builder.establish()

                if (vpnInterface != null) {
                    updateState(VpnState.Connected(serverName))
                    startForeground(
                        NOTIFICATION_ID,
                        buildNotification("اتصال برقرار است • مش وی پی ان", serverName, isConnected = true)
                    )
                    
                    // Maintain tunnel socket protection & loop
                    runTunnelLoop(config)
                } else {
                    updateState(VpnState.Error("خطا در ایجاد تونل VPN دستگاه"))
                    stopSelf()
                }
            } catch (e: Exception) {
                updateState(VpnState.Error(e.message ?: "خطای ناشناخته در اتصال"))
                stopSelf()
            }
        }
    }

    private suspend fun runTunnelLoop(config: ParsedOvpnConfig) = withContext(Dispatchers.IO) {
        try {
            val tunnelChannel = DatagramChannel.open()
            protect(tunnelChannel.socket())
            tunnelChannel.connect(InetSocketAddress(config.remoteHost, config.remotePort))
            tunnelChannel.configureBlocking(false)

            val vpnInput = FileInputStream(vpnInterface?.fileDescriptor).channel
            val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor).channel

            while (isActive && vpnInterface != null) {
                delay(1000)
            }
        } catch (e: Exception) {
            if (isActive) {
                updateState(VpnState.Error("ارتباط با سرور قطع شد"))
            }
        }
    }

    private fun stopVpnTunnel() {
        serviceJob.cancel()
        serviceJob = SupervisorJob()
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (_: Exception) {}

        updateState(VpnState.Disconnected)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(
        statusText: String,
        serverName: String,
        isConnected: Boolean = false
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, MashVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val pDisconnect = PendingIntent.getService(
            this, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, MashVpnApp.VPN_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_shield)
            .setContentTitle(serverName)
            .setContentText(statusText)
            .setContentIntent(pOpenApp)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isConnected) {
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "قطع اتصال",
                pDisconnect
            )
        }

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpnTunnel()
    }
}
