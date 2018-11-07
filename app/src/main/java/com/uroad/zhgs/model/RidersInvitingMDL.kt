package com.uroad.zhgs.model

/**
 * @author MFB
 * @create 2018/10/22
 * @describe 上次组队好友列表、关注列表等数据
 */
class RidersInvitingMDL {
    var lastteammember: MutableList<Riders>? = null
    var follow: MutableList<Riders>? = null

    class RiderType : MutilItem {
        var text: CharSequence? = null
        override fun getItemType(): Int = 0
    }

    class Riders : MutilItem {
        override fun getItemType(): Int = 1
        var userid: String? = null
        var username: String? = null
        var iconfile: String? = null
        var isInvitation: Int? = 0
        override fun equals(other: Any?): Boolean {
            return when (other) {
                !is Riders -> false
                else -> this === other || userid == other.userid
            }
        }

        override fun hashCode(): Int {
            return 31 + (userid?.hashCode() ?: 0)
        }
    }
}