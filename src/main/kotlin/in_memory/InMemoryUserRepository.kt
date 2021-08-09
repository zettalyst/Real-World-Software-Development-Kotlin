package in_memory

import FollowStatus
import User
import UserRepository

class InMemoryUserRepository : UserRepository {
    private val userIdToUser = hashMapOf<String, User>()

    override fun get(userId: String): User? {
        return userIdToUser[userId]
    }

    override fun add(user: User): Boolean {
        return userIdToUser.putIfAbsent(user.id, user) == null
    }

    override fun update(user: User) {
        // Deliberately blank - since we don't actually persist this data
    }

    override fun follow(follower: User, userToFollow: User): FollowStatus {
        return userToFollow.addFollower(follower)
    }

    override fun clear() {
        userIdToUser.clear()
    }

    override fun close() {
    }

}