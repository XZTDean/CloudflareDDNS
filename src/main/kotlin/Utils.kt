import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object Utils {
    fun HttpClient.send(
        request: HttpRequest, bodyHandler: HttpResponse.BodyHandler<String>, retry: Int
    ): HttpResponse<String> {
        return try {
            send(request, bodyHandler)
        } catch (e: IOException) {
            if (retry > 0) {
                Logger.log("Failed to send request to ${request.uri().toASCIIString()}, retrying...")
                send(request, bodyHandler, retry - 1)
            } else {
                throw e
            }
        }
    }
}