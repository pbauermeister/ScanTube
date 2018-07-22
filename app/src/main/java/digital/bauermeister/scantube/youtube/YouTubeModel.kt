package digital.bauermeister.scantube.youtube

object YouTubeSearchPlaylistResponse {
    data class Data(val items: Array<Item>)
    data class Item(val id: Id)
    data class Id(val playlistId: String)
}

object YouTubePlaylistItemsResponse {
    data class Data(val items: Array<Item>)
    data class Item(val contentDetails: ContentDetails)
    data class ContentDetails(val videoId: String)
}