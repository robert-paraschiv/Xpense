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

    const userID = snap.before.uid;

    if (picBefore != picAfter || nameBefore != nameAfter) {

        return admin.firestore().collection("Conversations").where("participants_phone_number_list", "array-contains", userID).get()
        .then(querySnapshot => {

            querySnapshot.forEach(doc => {
                let participants_users_list = doc.data().participants_users_list;

                participants_users_list.forEach(element => {
                    if (element.phoneNumber === userID) {
                        element.name = nameAfter;
                        element.pictureUrl = picAfter;
                        element.token = tokenAfter;
                    }
                });

                batch.update(doc.ref, { "participants_users_list": participants_users_list, "updatedByFunctions": true });

            });

        }).then(() => {
            batch.commit();
            return console.log("Successfully updated conversations for user " + userID);
        });

    } else {
        return console.log("Nothing to change for user");
    }


});