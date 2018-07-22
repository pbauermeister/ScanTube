package digital.bauermeister.scantube.googlevision

object GoogleVisionRequest {
    data class Data(val requests: Array<Request>)
    data class Request(val image: Image, val features: Array<Feature>)
    data class Image(val content: String)
    data class Feature(val type: String, val maxResults: Int)
}

object GoogleVisionWebDetectionResponse {
    data class Data(val responses: Array<Response>)
    data class Response(val webDetection: WebDetection)
    data class WebDetection(val bestGuessLabels: Array<BestGuessLabel>)
    data class BestGuessLabel(val languageCode: String, val label: String)
}

fun makeGoogleVisionWebDetectionRequest(imageBase64: String): GoogleVisionRequest.Data {
    return GoogleVisionRequest.Data(arrayOf(
            GoogleVisionRequest.Request(
                    GoogleVisionRequest.Image(imageBase64),
                    arrayOf(GoogleVisionRequest.Feature("WEB_DETECTION", 5))
            ))
    )
}