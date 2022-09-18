package com.rokudo.xpense.utils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.rokudo.xpense.models.User;

import java.util.Objects;

public class UserUtils {

//    @Nullable
//    public static User getOtherUser(Conversation conversation) {
//        User newOtherUser = null;
//        for (User user : conversation.getParticipants_users_list()) {
//            if (user.getPhoneNumber().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()))
//                continue;
//            newOtherUser = user;
//        }
//        return newOtherUser;
//    }

    public static boolean checkIfUserPicIsDifferent(User newOtherUser, User oldOtherUser) {
        if (newOtherUser != null && oldOtherUser != null) {
            if (oldOtherUser.getPictureUrl() == null && newOtherUser.getPictureUrl() != null) {
                return true;
            } else if (oldOtherUser.getPictureUrl() != null && newOtherUser.getPictureUrl() == null) {
                return true;
            } else
                return (oldOtherUser.getPictureUrl() != null && newOtherUser.getPictureUrl() != null)
                        && !oldOtherUser.getPictureUrl().equals(newOtherUser.getPictureUrl());
        }
        return false;
    }
}
