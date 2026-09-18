package com.mashvpn.android.data.remote

import com.mashvpn.android.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SupabaseApiService {

    @POST("rest/v1/rpc/verify_and_register_ovpn_device")
    async fun verifyLicense(
        @Body request: LicenseVerifyRequest
    ): Response<LicenseVerifyResponse>

    @POST("rest/v1/rpc/get_ovpn_active_nodes")
    async fun getActiveNodes(
        @Body request: GetNodesRequest
    ): Response<List<VpnNode>>

    @POST("rest/v1/rpc/ovpn_heartbeat")
    async fun sendHeartbeat(
        @Body request: HeartbeatRequest
    ): Response<HeartbeatResponse>
}
