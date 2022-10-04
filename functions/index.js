const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase)

exports.transactionInsertionListener = functions.firestore.document("Transactions/{transactionID}").onCreate((snap, context) => {

    const transaction = snap.data();
    if (transaction.type == "Income") {
        return admin.firestore().collection("Wallets").doc(transaction.walletId).update({
            amount: admin.firestore.FieldValue.increment(transaction.amount)
        });
    } else {
        return admin.firestore().collection("Wallets").doc(transaction.walletId).update({
            amount: admin.firestore.FieldValue.increment(-transaction.amount)
        });
    }

});

exports.profilePictureChangeListener = functions.firestore.document("Users/{userPhoneNumber}").onWrite((snap, context) => {

    const userPhoneNumber = context.params.userPhoneNumber;

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

                    walletUsers.forEach(element => {
                        if (element.userId === userID) {
                            element.userPic = picAfter;
                        }
                    });

                    batch.update(doc.ref, { "walletUsers": walletUsers });

                });

            }).then(() => {
                return admin.firestore().collection("Transactions").where("user_id", "==", userID).get()
                    .then(querySnapshot => {

                        querySnapshot.forEach(doc => {
                            batch.update(doc.ref, { "picUrl": picAfter });
                        });

                    }).then(() => {
                        batch.commit();
                        return console.log("Successfully updated wallets and transactions for user " + userID);
                    });

            });


    } else {
        return console.log("Nothing to change for user");
    }

});