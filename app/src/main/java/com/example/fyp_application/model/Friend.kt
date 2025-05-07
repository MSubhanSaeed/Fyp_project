sealed class FriendItem {
    data class Friend(val name: String, var isTrackingAllowed: Boolean) : FriendItem()
    object AddButton : FriendItem()
}