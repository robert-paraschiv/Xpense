const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { firestore } = require('firebase-admin');
admin.initializeApp(functions.config().firebase)

exports.invitesListener = functions.firestore.document("Invitations/{invitationID}").onWrite((snap, context) => {
    const invitationID = context.params.invitationID;

    if (!snap.after.exists) {
        //Invitation was deleted by server or user
        return 0;
    }

    var isNewTrans = true;
    if (snap.before.exists) {
        isNewTrans = false;
    }


    const newInvite = snap.after.exists ? snap.after.data() : null;
    const oldInvite = snap.before.exists ? snap.before.data() : null;

    let receiverPhoneNumber = newInvite.invited_person_phone_number;

    if (isNewTrans) {
        //Send notification

        return admin.firestore().collection("Users").doc(receiverPhoneNumber)
            .get()
            .then(documentSnapshot => {
                let user = documentSnapshot.data();

                if (user == null) {
                    return console.log("User was null");
                } else {
                    const messsage = {
                        data: {
                            "senderName": String(newInvite.creator_name),
                            "senderPictureUrl": String(newInvite.creator_pic_url),
                            "walletTitle": String(newInvite.wallet_title),
                            "walletID": String(invitationID)
                        },
                        token: user.token
                    }
                    return admin.messaging().send(messsage).then(result => {
                        return console.log("Notification sent ");
                    });

                }
            });

    } else {
        if (newInvite.status == "Accepted") {
            //Add to wallet
            return admin.firestore().collection("Users").doc(receiverPhoneNumber)
                .get()
                .then(documentSnapshot => {
                    let user = documentSnapshot.data();

                    if (user == null) {
                        return console.log("User was null");
                    } else {
                        const receiverUser = {
                            userId: user.uid,
                            userName: user.name,
                            userPic: user.pictureUrl
                        };
                        const senderUser = {
                            userId: newInvite.creator_id,
                            userName: newInvite.creator_name,
                            userPic: newInvite.creator_pic_url
                        };
                        const walletUsers = [receiverUser, senderUser];
                        const users = [senderUser.userId, user.uid];

                        return admin.firestore().collection("Wallets").doc(invitationID).update({
                            "users": users,
                            "walletUsers": walletUsers
                        });
                    }
                });

        } else {
            return 0;
        }
    }

});

exports.transactionInsertionListener = functions.firestore.document("Transactions/{transactionID}").onWrite((snap, context) => {
    //Don't do anything if it is a cash transaction
    if (snap.after.data().cashTransaction == true) {
        return 0;
    }

    var isNewTrans = true;
    if (snap.before.exists) {
        isNewTrans = false;
    }



    if (isNewTrans) {
        const transaction = snap.after.exists ? snap.after.data() : null;
        if (transaction == null) {
            return 0;
        }

        if (transaction.type == "Income") {
            return admin.firestore().collection("Wallets").doc(transaction.walletId).update({
                amount: admin.firestore.FieldValue.increment(transaction.amount)
            });
        } else {
            return admin.firestore().collection("Wallets").doc(transaction.walletId).update({
                amount: admin.firestore.FieldValue.increment(-transaction.amount)
            });
        }
    } else {
        const transBefore = snap.before.exists ? snap.before.data() : null;
        const transAfter = snap.after.exists ? snap.after.data() : null;

        if (transBefore.type == transAfter.type) {

            if (transBefore != null && transAfter != null) {
                if (transAfter.amount != transBefore.amount) {

                    if (transAfter.type == "Expense") {

                        if (transBefore.amount < transAfter.amount) {
                            var decrement = transAfter.amount - transBefore.amount;

                            return admin.firestore().collection("Wallets").doc(transAfter.walletId).update({
                                amount: admin.firestore.FieldValue.increment(-decrement)
                            });
                        } else {
                            var increment = transBefore.amount - transAfter.amount;

                            return admin.firestore().collection("Wallets").doc(transAfter.walletId).update({
                                amount: admin.firestore.FieldValue.increment(increment)
                            });
                        }
                    }
                }
            }

        }

        return 0;
    }

});

exports.profilePictureChangeListener = functions.firestore.document("Users/{userPhoneNumber}").onWrite((snap, context) => {

    const documentAfter = snap.after.exists ? snap.after.data() : null;
    const documentBefore = snap.before.exists ? snap.before.data() : null;

    const picBefore = documentBefore.pictureUrl;
    const picAfter = documentAfter.pictureUrl;
    const nameBefore = documentBefore.name;
    const nameAfter = documentAfter.name;

    const userID = documentBefore.uid;

    if (picBefore != picAfter || nameBefore != nameAfter) {
        const batch = admin.firestore().batch();
        return admin.firestore().collection("Wallets").where("users", "array-contains", userID).get()
            .then(querySnapshot => {

                querySnapshot.forEach(doc => {
                    let walletUsers = doc.data().walletUsers;

                    if (walletUsers != null) {
                        walletUsers.forEach(element => {
                            if (element.userId === userID) {
                                element.userPic = picAfter;
                                element.userName = nameAfter;
                            }
                        });
                    }

                    batch.update(doc.ref, { "walletUsers": walletUsers });

                });

            }).then(() => {
                batch.commit();
                return console.log("Successfully updated wallets and transactions for user " + userID);
            });

    } else {
        return console.log("Nothing to change for user");
    }

});