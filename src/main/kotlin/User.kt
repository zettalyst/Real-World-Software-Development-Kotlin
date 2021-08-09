import java.util.stream.Stream

class User(
    val id: String,
    val password: ByteArray,
    val salt: ByteArray,
    var lastSeenPosition: Position,
) {
    val followers: HashSet<User> = hashSetOf()
    val following: HashSet<String> = hashSetOf()
    var receiverEndPoint: ReceiverEndPoint? = null
    fun receiveTwoot(twoot: Twoot): Boolean {
        if (isLoggedOn()) {
            receiverEndPoint?.onTwoot(twoot)
            lastSeenPosition = twoot.position
            return true
        }
        return false
    }

    fun isLoggedOn(): Boolean {
        return receiverEndPoint != null
    }

    fun addFollower(user: User): FollowStatus {
        if (followers.add(user)) {
            user.following.add(id)
            return FollowStatus.SUCCESS;
        } else {
            return FollowStatus.ALREADY_FOLLOWING;
        }
    }

    fun onLogon(receiverEndPoint: ReceiverEndPoint) {
        this.receiverEndPoint = receiverEndPoint
    }

    fun onLogoff() {
        receiverEndPoint = null
    }

    fun followers(): Stream<User> {
        return followers.stream()
    }

    fun getFollowing(): Set<String> {
        return following
    }
}