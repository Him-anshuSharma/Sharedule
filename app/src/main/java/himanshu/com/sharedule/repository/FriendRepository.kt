package himanshu.com.sharedule.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import himanshu.com.sharedule.model.Friend
import himanshu.com.sharedule.model.FriendRequest
import kotlinx.coroutines.tasks.await
import android.util.Log

class FriendRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val USERS = "users"
        private const val FRIEND_REQUESTS = "friend_requests"
        private const val FRIENDS = "friends"
        private const val NO_UID_ERROR = "UID is null"
        private const val DEFAULT_STATUS = "pending"
    }


    private val uid: String? get() = auth.uid
    private val name: String? get() = auth.currentUser?.displayName

    /**
     * Accepts a friend request by adding both users to each other's friends subcollection.
     */
    suspend fun acceptRequest(request: FriendRequest): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception(NO_UID_ERROR))
        return try {
            // Fetch display names
            // Add each other as friends with display names
            db.collection(USERS).document(request.from).collection(FRIENDS).add(Friend(uid.toString(),
                name.toString()
            )).await()
            db.collection(USERS).document(currentUid).collection(FRIENDS).add(Friend(request.from,request.displayName)).await()


            // Update the friend request status to 'accepted' in the current user's friend_requests collection
            val requestsQuery = db.collection(USERS)
                .document(currentUid)
                .collection(FRIEND_REQUESTS)
                .whereEqualTo("from", request.from)
                .whereEqualTo("status", DEFAULT_STATUS)
                .get()
                .await()
            for (doc in requestsQuery.documents) {
                doc.reference.update("status", "accepted").await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a friend request to another user.
     */
    suspend fun sendRequest(friendUid: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception(NO_UID_ERROR))
        return try {
            val requestData = FriendRequest(currentUid, DEFAULT_STATUS,auth.currentUser?.displayName.toString())
            db.collection(USERS).document(friendUid)
                .collection(FRIEND_REQUESTS)
                .add(requestData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns the list of friends for the current user.
     */
    suspend fun getFriendList(): List<Friend> {
        val currentUid = uid ?: return emptyList()
        return try {
            val snapshot = db.collection(USERS)
                .document(currentUid)
                .collection(FRIENDS)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Friend::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Returns the list of pending friend requests for the current user.
     */
    suspend fun getPendingRequests(): List<FriendRequest> {
        val currentUid = uid ?: return emptyList()
        return try {
            val snapshot = db.collection(USERS)
                .document(currentUid)
                .collection(FRIEND_REQUESTS)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(FriendRequest::class.java) }
        } catch (e: Exception) {
            Log.d("Sharedule-Debug",e.message.toString())
            emptyList<FriendRequest>()
        }
    }

    /**
     * Returns a reference to the current user's friends subcollection.
     */
    fun getFriendListDocument(): CollectionReference? {
        val currentUid = uid
        return if (currentUid != null) {
            db.collection(USERS).document(currentUid).collection(FRIENDS)
        } else {
            null
        }
    }
}
