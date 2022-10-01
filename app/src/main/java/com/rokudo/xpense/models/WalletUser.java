package com.rokudo.xpense.models;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

public class WalletUser {
    private String userId;
    private String userPic;

    public WalletUser() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalletUser that = (WalletUser) o;
        return userId.equals(that.userId) && Objects.equals(userPic, that.userPic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    public static String getOtherUserProfilePic(List<WalletUser> walletUsers) {
        if (walletUsers == null)
            return null;
        for (WalletUser walletUser : walletUsers) {
            if (!walletUser.getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                return walletUser.getUserPic();
            }
        }
        return null;
    }
}
