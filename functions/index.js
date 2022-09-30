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