import com.google.gson.Gson
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
    }

    private fun getRecordId(): String? {
        val url = baseUrl.format(Config.zone_id, "")
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Auth-Email", Config.api_email)
            .header("Authorization", "Bearer ${Config.api_key}")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            // Log error
            return ""
        }

        val dnsRecords = gson.fromJson(response.body(), DnsRecordListResponse::class.java)
        if (!dnsRecords.success) {
            // Log error
            return ""
        }

        val record = dnsRecords.result.find { it.name == Config.domain }
        return record?.id
    }

    class DnsRecord {
        val id: String = ""
        val content: String = ""
        val name: String = ""
        val proxied: Boolean = false
        val type: String = "A"
        val ttl: Int = 1
    }

    class DnsRecordListResponse {
        val success: Boolean = false
        val result: List<DnsRecord> = emptyList()
    }
}