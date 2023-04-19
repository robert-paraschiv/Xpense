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

exports.testTransactionListener = functions.firestore.document("TestTransactions/{transactionID}").onWrite((snap, context) => {
    const transaction = snap.after.exists ? snap.after.data() : null;

    //Transaction was deleted
    if (transaction == null) {
        return 0;
    }

    const transactionDate = transaction.date.toDate();
    const months = ["January", "February", "March", "April", "May", "June", "July", "August",
        "September", "October", "November", "December"];
    const doc = admin.firestore()
        .collection("Wallets")
        .doc(transaction.walletId)
        .collection("Statistics")
        .doc('' + transactionDate.getFullYear())
        .collection("Months")
        .doc(months[transactionDate.getMonth()]);

    const transactionDay = transactionDate.getDate();


    return doc.get().then((snap) => {
        if (snap.exists) {
            return updateMonthDocument(transaction, transactionDay, doc);
        } else {
            return createMonthDocument(transaction, doc, transactionDay);
        }
    });


});

exports.transactionInsertionListener = functions.firestore.document("Transactions/{transactionID}").onWrite((snap, context) => {
    //Don't do anything if it is a cash transaction
    if (snap.after.exists && snap.after.data().cashTransaction == true) {
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

        if (transAfter == null) {
            //Transaction was deleted
            if (transBefore.type == "Expense") {
                return admin.firestore().collection("Wallets").doc(transBefore.walletId).update({
                    amount: admin.firestore.FieldValue.increment(transBefore.amount)
                });
            } else {
                return admin.firestore().collection("Wallets").doc(transBefore.walletId).update({
                    amount: admin.firestore.FieldValue.increment(-transBefore.amount)
                });
            }

        }

        if (transAfter.amount == transBefore.amount) {
            if (transBefore.type == transAfter.type) {
                return 0;
            } else {
                if (transAfter.type == "Expense") {
                    return admin.firestore().collection("Wallets").doc(transAfter.walletId).update({
                        amount: admin.firestore.FieldValue.increment(-transAfter.amount)
                    });
                } else {
                    return admin.firestore().collection("Wallets").doc(transAfter.walletId).update({
                        amount: admin.firestore.FieldValue.increment(transAfter.amount)
                    });
                }
            }

        } else {

            if (transBefore.type == transAfter.type) {

                if (transBefore.type == "Expense") {
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

                } else {
                    if (transBefore.amount < transAfter.amount) {
                        var increment = transAfter.amount - transBefore.amount;

                        return admin.firestore().collection("Wallets").doc(transAfter.walletId).update({
                            amount: admin.firestore.FieldValue.increment(increment)
                        });
                    } else {
                        var decrement = transBefore.amount - transAfter.amount;

                        return admin.firestore().collection("Wallets").doc(transAfter.walletId).update({
                            amount: admin.firestore.FieldValue.increment(-decrement)
                        });
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

function createMonthDocument(transaction, doc, transactionDay) {
    const transactionToAdd = {
        id: transaction.id,
        amount: transaction.amount,
        title: transaction.title,
        currency: transaction.currency,
        date: transaction.date,
        dateLong: transaction.dateLong,
        picUrl: transaction.picUrl,
        type: transaction.type,
        userName: transaction.userName,
        user_id: transaction.user_id,
        walletId: transaction.walletId
    };

    return doc.set({
        transactions: { [transaction.id]: transactionToAdd },
        categories: { [transaction.category]: transactionToAdd },
        amountByCategory: { [transaction.category]: transaction.amount },
        transactionsByDay: { [transactionDay]: transactionToAdd },
        monthTotalAmount: transaction.amount
    });
}

function updateMonthDocument(transaction, transactionDay, doc) {
    const idField = `transactions.${transaction.id}.id`;
    const titleField = `transactions.${transaction.id}.title`;
    const amountField = `transactions.${transaction.id}.amount`;
    const currencyField = `transactions.${transaction.id}.currency`;
    const dateField = `transactions.${transaction.id}.date`;
    const dateLongField = `transactions.${transaction.id}.dateLong`;
    const picUrlField = `transactions.${transaction.id}.picUrl`;
    const typeField = `transactions.${transaction.id}.type`;
    const userNameField = `transactions.${transaction.id}.userName`;
    const user_idField = `transactions.${transaction.id}.user_id`;
    const walletIdField = `transactions.${transaction.id}.walletId`;

    const dayIdField = `transactionsByDay.${transactionDay}.${transaction.id}.id`;
    const dayTitleField = `transactionsByDay.${transactionDay}.${transaction.id}.title`;
    const dayAmountField = `transactionsByDay.${transactionDay}.${transaction.id}.amount`;
    const dayCurrencyField = `transactionsByDay.${transactionDay}.${transaction.id}.currency`;
    const dayDateField = `transactionsByDay.${transactionDay}.${transaction.id}.date`;
    const dayDateLongField = `transactionsByDay.${transactionDay}.${transaction.id}.dateLong`;
    const dayPicUrlField = `transactionsByDay.${transactionDay}.${transaction.id}.picUrl`;
    const dayTypeField = `transactionsByDay.${transactionDay}.${transaction.id}.type`;
    const dayUserNameField = `transactionsByDay.${transactionDay}.${transaction.id}.userName`;
    const dayUser_idField = `transactionsByDay.${transactionDay}.${transaction.id}.user_id`;
    const dayWalletIdField = `transactionsByDay.${transactionDay}.${transaction.id}.walletId`;

    const categoryIdField = `categories.${transaction.category}.${transaction.id}.id`;
    const categoryTitleField = `categories.${transaction.category}.${transaction.id}.title`;
    const categoryAmountField = `categories.${transaction.category}.${transaction.id}.amount`;
    const categoryCurrencyField = `categories.${transaction.category}.${transaction.id}.currency`;
    const categoryDateField = `categories.${transaction.category}.${transaction.id}.date`;
    const categoryDateLongField = `categories.${transaction.category}.${transaction.id}.dateLong`;
    const categoryPicUrlField = `categories.${transaction.category}.${transaction.id}.picUrl`;
    const categoryTypeField = `categories.${transaction.category}.${transaction.id}.type`;
    const categoryUserNameField = `categories.${transaction.category}.${transaction.id}.userName`;
    const categoryUser_idField = `categories.${transaction.category}.${transaction.id}.user_id`;
    const categoryWalletIdField = `categories.${transaction.category}.${transaction.id}.walletId`;

    const categoriesByAmountField = `amountByCategory.${transaction.category}`;

    return doc.update({
        monthTotalAmount: admin.firestore.FieldValue.increment(transaction.amount),
        [categoriesByAmountField]: admin.firestore.FieldValue.increment(transaction.amount),

        [idField]: transaction.id,
        [titleField]: transaction.title,
        [amountField]: transaction.amount,
        [currencyField]: transaction.currency,
        [dateField]: transaction.date,
        [dateLongField]: transaction.dateLong,
        [picUrlField]: transaction.picUrl,
        [typeField]: transaction.type,
        [userNameField]: transaction.userName,
        [user_idField]: transaction.user_id,
        [walletIdField]: transaction.walletId,

        [categoryIdField]: transaction.id,
        [categoryTitleField]: transaction.title,
        [categoryAmountField]: transaction.amount,
        [categoryCurrencyField]: transaction.currency,
        [categoryDateField]: transaction.date,
        [categoryDateLongField]: transaction.dateLong,
        [categoryPicUrlField]: transaction.picUrl,
        [categoryTypeField]: transaction.type,
        [categoryUserNameField]: transaction.userName,
        [categoryUser_idField]: transaction.user_id,
        [categoryWalletIdField]: transaction.walletId,

        [dayIdField]: transaction.id,
        [dayTitleField]: transaction.title,
        [dayAmountField]: transaction.amount,
        [dayCurrencyField]: transaction.currency,
        [dayDateField]: transaction.date,
        [dayDateLongField]: transaction.dateLong,
        [dayPicUrlField]: transaction.picUrl,
        [dayTypeField]: transaction.type,
        [dayUserNameField]: transaction.userName,
        [dayUser_idField]: transaction.user_id,
        [dayWalletIdField]: transaction.walletId
    });
}
