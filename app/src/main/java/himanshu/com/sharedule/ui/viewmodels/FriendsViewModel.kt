package himanshu.com.sharedule.ui.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import himanshu.com.sharedule.model.Friend
import himanshu.com.sharedule.model.FriendRequest
import himanshu.com.sharedule.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration

class FriendViewModel(
    private val context:Context,
) : ViewModel() {

    internal val repository = FriendRepository()

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _friendRequests =  MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    private var friendsListener: ListenerRegistration? = null

    init {
        observeFriendsRealtime()
    }

    /** Observes the friends collection in Firestore in real time. */
    private fun observeFriendsRealtime() {
        friendsListener?.remove()
        friendsListener = repository.getFriendListDocument()?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(context,error.message, Toast.LENGTH_SHORT)
            }
            if (snapshot != null) {
                val friendsList = snapshot.documents.mapNotNull { it.toObject(Friend::class.java) }
                _friends.value = friendsList
            }
        }
    }

    fun getFriendRequests(){
        viewModelScope.launch {
            val data = repository.getPendingRequests()
            _friendRequests.value = data
        }
    }

    /** Sends a friend request to another user. */
    fun sendFriendRequest(friendUid: String) {
        viewModelScope.launch {
            repository.sendRequest(friendUid)
        }
    }

    /** Accepts a friend request and updates both users' friends collections. */
    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            repository.acceptRequest(request)
        }
    }

    /** Optionally: Rejects a friend request (implement in repository if needed). */
    fun rejectFriendRequest(requestId: String) {
        // Implement reject logic if your repository supports it
    }

    fun refreshPendingRequests() {
        viewModelScope.launch {
            // The repository will handle fetching pending requests
        }
    }

    override fun onCleared() {
        super.onCleared()
        friendsListener?.remove()
    }
}
