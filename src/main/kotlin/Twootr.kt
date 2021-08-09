import Position.Companion.INITIAL_POSITION

class Twootr(val userRepository: UserRepository, val twootRepository: TwootRepository) {
    fun onLogon(userId: String, password: String, receiverEndPoint: ReceiverEndPoint): SenderEndPoint? {
        val authenticatedUser = userRepository
            .get(userId)?.takeIf {
                val hashedPassword = KeyGenerator.hash(password, it.salt)
                hashedPassword.contentEquals(it.password)
            }

        authenticatedUser?.let { user ->
            user.onLogon(receiverEndPoint)
            twootRepository.query(
                TwootQuery()
                    .inUsers(user.getFollowing())
                    .lastSeenPosition(user.lastSeenPosition),
                user::receiveTwoot
            )
            userRepository.update(user)
        }

        return authenticatedUser?.let { SenderEndPoint(it, this) }
    }

    fun onRegisterUser(userId: String, password: String): RegistrationStatus? {
        val salt = KeyGenerator.newSalt()
        val hashedPassword = KeyGenerator.hash(password, salt)
        val user = User(userId, hashedPassword, salt, INITIAL_POSITION)
        return if (userRepository.add(user)) RegistrationStatus.SUCCESS else RegistrationStatus.DUPLICATE
    }

    fun onFollow(follow: User, userIdToFollow: String): FollowStatus {
        return userRepository.get(userIdToFollow)?.let {
            userRepository.follow(follow, it)
        } ?: FollowStatus.INVALID_USER
    }

    fun onSendTwoot(id: String, user: User, content: String): Position {
        val userId = user.id
        val twoot = twootRepository.add(id, userId, content)
        user.followers.filter { it.isLoggedOn() }.forEach {
            it.receiveTwoot(twoot)
            userRepository.update(it)
        }

        return twoot.position
    }

    fun onDeleteTwoot(userId: String, id: String): DeleteStatus {
        return twootRepository.get(id)?.let {
            val canDeleteTwoot = it.senderId.equals(userId)
            if (canDeleteTwoot) {
                twootRepository.delete(it)
                DeleteStatus.SUCCESS
            } else {
                DeleteStatus.NOT_YOUR_TWOOT
            }
        } ?: DeleteStatus.UNKNOWN_TWOOT
    }
}