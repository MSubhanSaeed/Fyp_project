
sealed class FriendItem {
    data class Friend(val id:Int,val name: String, var isTrackingAllowed: Boolean) : FriendItem()
}