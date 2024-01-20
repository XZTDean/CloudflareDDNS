import com.google.gson.Gson
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class DdnsUpdater {
    private val gson = Gson()
    private val baseUrl = "https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s"
    private val client = HttpClient.newHttpClient()

    fun updateDns(ip: String) {
        val recordId = getRecordId()
        val dnsRecord = DnsRecord(recordId, ip, Config.domain)
        if (recordId == null) {
            createNewRecord(dnsRecord)
        } else {
            updateRecord(dnsRecord, recordId)
        }
    }

    private fun getRecordId(): String? {
        val url = baseUrl.format(Config.zone_id, "")
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Auth-Email", Config.api_email)
            .header("Authorization", "Bearer ${Config.api_key}")
            .build()
        val response = sendRequest(request)
        if (response.statusCode() != 200) {
            Logger.log("Failed to get DNS record ID")
            Logger.log("API status code: ${response.statusCode()}")
            Logger.log("API response: ${response.body()}")
            Logger.saveLog()
            return ""
        }

        val dnsRecords = gson.fromJson(response.body(), DnsRecordListResponse::class.java)
        if (!dnsRecords.success) {
            Logger.log("Failed to get DNS record ID")
            Logger.log("API errors:")
            dnsRecords.errors.forEach {
                Logger.log(" ${it.code}: ${it.message}")
            }
            Logger.saveLog()
            return ""
        }

        val record = dnsRecords.result.find { it.name == Config.domain }
        return record?.id
    }

    private fun createNewRecord(record: DnsRecord) {
        val url = baseUrl.format(Config.zone_id, "")
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Auth-Email", Config.api_email)
            .header("Authorization", "Bearer ${Config.api_key}")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(record)))
            .build()
        val response = sendRequest(request)
        if (response.statusCode() != 200) {
            Logger.log("Failed to create new DNS record")
            Logger.log("API status code: ${response.statusCode()}")
            Logger.log("API response: ${response.body()}")
            Logger.saveLog()
            return
        }

        val dnsRecords = gson.fromJson(response.body(), DnsRecordDetailResponse::class.java)
        if (!dnsRecords.success) {
            Logger.log("Failed to create new DNS record")
            Logger.log("API errors:")
            dnsRecords.errors.forEach {
                Logger.log("  ${it.code}: ${it.message}")
            }
            Logger.saveLog()
            return
        }
        if (dnsRecords.result.content != record.content || dnsRecords.result.name != record.name) {
            Logger.log("Failed to create new DNS record")
            Logger.log("Created record does not match with the request")
            Logger.log("Result: ${dnsRecords.result}")
            Logger.saveLog()
            return
        }
    }

    private fun updateRecord(record: DnsRecord, recordId: String) {
        val url = baseUrl.format(Config.zone_id, recordId)
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Auth-Email", Config.api_email)
            .header("Authorization", "Bearer ${Config.api_key}")
            .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(record)))
            .build()
        val response = sendRequest(request)
        if (response.statusCode() != 200) {
            Logger.log("Failed to update DNS record")
            Logger.log("API status code: ${response.statusCode()}")
            Logger.log("API response: ${response.body()}")
            Logger.saveLog()
            return
        }

        val dnsRecords = gson.fromJson(response.body(), DnsRecordDetailResponse::class.java)
        if (!dnsRecords.success) {
            Logger.log("Failed to update DNS record")
            Logger.log("API errors:")
            dnsRecords.errors.forEach {
                Logger.log("  ${it.code}: ${it.message}")
            }
            Logger.saveLog()
            return
        }
        if (dnsRecords.result != record) {
            Logger.log("Failed to update DNS record")
            Logger.log("Updated record does not match with the request")
            Logger.log("Result: ${dnsRecords.result}")
            Logger.saveLog()
            return
        }
    }

    private fun sendRequest(request: HttpRequest?): HttpResponse<String> {
        val response: HttpResponse<String> = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: IOException) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        return response
    }

    data class DnsRecord(
        val id: String? = null,
        val content: String = "",
        val name: String = "",
        val proxied: Boolean = false,
        val type: String = "A",
        val ttl: Int = 1
    )

    data class DnsRecordListResponse(
        val success: Boolean = false,
        val result: List<DnsRecord> = emptyList(),
        val errors: List<Error> = emptyList()
    )

    data class DnsRecordDetailResponse(
        val success: Boolean = false,
        val result: DnsRecord = DnsRecord(),
        val errors: List<Error> = emptyList()
    )

    data class Error(
        val code: Int = 0,
        val message: String = ""
    )
}