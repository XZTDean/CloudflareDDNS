import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class IpChecker {
    private var ip = ""
    private val url = "http://ip-api.com/json/?fields=status,message,query"

    private val client = HttpClient.newHttpClient()
    private val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
    private val gson = Gson()

    fun getIp(): String {
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            Logger.log("Failed to get IP address")
            Logger.log("API status code: ${response.statusCode()}")
            Logger.log("API response: ${response.body()}")
            Logger.saveLog()
            return ip
        }

        val ipResponse = gson.fromJson(response.body(), IpResponse::class.java)
        if (ipResponse.status != "success") {
            Logger.log("Failed to get IP address")
            Logger.log("API status: ${ipResponse.status}")
            Logger.log("API message: ${ipResponse.message}")
            Logger.saveLog()
            return ip
        }

        ip = ipResponse.query ?: ""
        return ip
    }

    class IpResponse {
        val status: String = ""
        val message: String? = null
        val query: String? = null
    }
}