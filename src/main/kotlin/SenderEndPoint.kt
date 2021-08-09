class SenderEndPoint(val user: User, val twootr: Twootr) {
    fun onFollow(userIdToFollow: String): FollowStatus {
        return twootr.onFollow(user, userIdToFollow)
    }

    fun onSendTwoot(id: String, content: String): Position {
        return twootr.onSendTwoot(id, user, content)
    }

    fun onLogoff() {
        user.onLogoff()
    }

    fun onDeleteTwoot(id: String): DeleteStatus {
        return twootr.onDeleteTwoot(user.id, id)
    }
}