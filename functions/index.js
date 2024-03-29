const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { firestore } = require('firebase-admin');
const months = ["January", "February", "March", "April", "May", "June", "July", "August",
    "September", "October", "November", "December"];
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

exports.transactionInsertionWalletSumUpdater = functions.firestore.document("Wallets/{walletId}/Transactions/{transactionID}").onWrite((snap, context) => {
    const updatedTransaction = snap.after.exists ? snap.after.data() : null;
    const oldTransaction = snap.before.exists ? snap.before.data() : null;

    const batch = admin.firestore().batch();

    if (updatedTransaction == null) {
        if (oldTransaction.cashTransaction !== true) {

            batch.update(admin.firestore().collection("Wallets").doc(oldTransaction.walletId), {
                amount: admin.firestore.FieldValue.increment(oldTransaction.type === "Expense" ? oldTransaction.amount : -oldTransaction.amount)
            });
        }
    } else {
        if (oldTransaction == null) {

            if (updatedTransaction.cashTransaction !== true) {
                batch.update(admin.firestore().collection("Wallets").doc(updatedTransaction.walletId), {
                    amount: admin.firestore.FieldValue.increment(updatedTransaction.type === "Income" ? updatedTransaction.amount : -updatedTransaction.amount)
                });
            }

        } else {
            const walletDoc = admin.firestore().collection("Wallets").doc(updatedTransaction.walletId);

            //TODO Need to check for transaction if it is cash or not
            if (updatedTransaction.amount == oldTransaction.amount) {
                if (oldTransaction.type == updatedTransaction.type) {
                    console.log("amount and type were the same");
                } else {

                    if (updatedTransaction.type == "Expense") {
                        batch.update(walletDoc, { amount: admin.firestore.FieldValue.increment(-updatedTransaction.amount) });
                    } else {
                        batch.update(walletDoc, { amount: admin.firestore.FieldValue.increment(updatedTransaction.amount) });
                    }
                }

            } else {

                if (oldTransaction.type == updatedTransaction.type) {

                    if (oldTransaction.type == "Expense") {
                        if (oldTransaction.amount < updatedTransaction.amount) {
                            var decrement = updatedTransaction.amount - oldTransaction.amount;

                            batch.update(walletDoc, { amount: admin.firestore.FieldValue.increment(-decrement) });
                        } else {
                            var increment = oldTransaction.amount - updatedTransaction.amount;

                            batch.update(walletDoc, { amount: admin.firestore.FieldValue.increment(increment) });
                        }

                    } else {
                        if (oldTransaction.amount < updatedTransaction.amount) {
                            var increment = updatedTransaction.amount - oldTransaction.amount;

                            batch.update(walletDoc, { amount: admin.firestore.FieldValue.increment(increment) });
                        } else {
                            var decrement = oldTransaction.amount - updatedTransaction.amount;

                            batch.update(walletDoc, { amount: admin.firestore.FieldValue.increment(-decrement) });
                        }
                    }
                } else {
                    console.log("unimplemented bruh");
                }
            }
        }
    }

    return batch.commit();
});

exports.transactionInsertionStatisticsGenerator = functions.firestore.document("Wallets/{walletId}/Transactions/{transactionID}").onWrite((snap, context) => {
    const updatedTransaction = snap.after.exists ? snap.after.data() : null;
    const oldTransaction = snap.before.exists ? snap.before.data() : null;

    const batch = admin.firestore().batch();

    if (updatedTransaction == null) {
        //Transaction was deleted

        const transactionDate = oldTransaction.date.toDate();
        const transactionDay = transactionDate.getDate();

        const categoriesByAmountField = `amountByCategory.${oldTransaction.category}`;

        const monthDocument = admin.firestore()
            .collection("Wallets")
            .doc(oldTransaction.walletId)
            .collection("Statistics")
            .doc('' + transactionDate.getFullYear())
            .collection("Months")
            .doc(months[transactionDate.getMonth()]);

        return Promise.all([monthDocument.get()])
            .then(results => {
                const monthDoc = results[0];

                if (monthDoc.exists) {
                    batch.update(monthDocument, {
                        latestUpdateTime: firestore.Timestamp.now(),
                        totalAmountSpent: admin.firestore.FieldValue.increment(oldTransaction.type === "Expense" ? -oldTransaction.amount : 0),
                        [categoriesByAmountField]: admin.firestore.FieldValue.increment(-oldTransaction.amount),

                        [`transactions.${oldTransaction.id}`]: admin.firestore.FieldValue.delete(),
                        [`categories.${oldTransaction.category}.${oldTransaction.id}`]: admin.firestore.FieldValue.delete(),
                        [`transactionsByDay.${transactionDay}.${oldTransaction.id}`]: admin.firestore.FieldValue.delete()
                    });
                }

                return batch.commit();
            });

    } else {
        //Transaction was either created or updated
        const transactionDate = updatedTransaction.date.toDate();
        const transactionDay = transactionDate.getDate();

        const monthDocument = admin.firestore()
            .collection("Wallets")
            .doc(updatedTransaction.walletId)
            .collection("Statistics")
            .doc('' + transactionDate.getFullYear())
            .collection("Months")
            .doc(months[transactionDate.getMonth()]);

        if (oldTransaction == null) {
            //transaction is new
            //Update wallet balance

            const categoriesByAmountField = `amountByCategory.${updatedTransaction.category}`;
            const categoryIdField = `categories.${updatedTransaction.category}.${updatedTransaction.id}`;
            const dayIdField = `transactionsByDay.${transactionDay}.${updatedTransaction.id}`;
            const idField = `transactions.${updatedTransaction.id}`;


            if (updatedTransaction.type === "Transfer") {
                return batch.commit();
            } else {
                return Promise.all([monthDocument.get()])
                    .then(results => {
                        const monthDoc = results[0];

                        if (monthDoc.exists) {
                            batch.update(monthDocument, {
                                latestUpdateTime: firestore.Timestamp.now(),
                                totalAmountSpent: admin.firestore.FieldValue.increment(updatedTransaction.type === "Expense" ? updatedTransaction.amount : 0),
                                [categoriesByAmountField]: admin.firestore.FieldValue.increment(updatedTransaction.amount),

                                [idField]: updatedTransaction,
                                [categoryIdField]: updatedTransaction,
                                [dayIdField]: updatedTransaction
                            });
                        } else {
                            batch.set(monthDocument, {
                                latestUpdateTime: firestore.Timestamp.now(),
                                transactions: { [updatedTransaction.id]: updatedTransaction },
                                categories: { [updatedTransaction.category]: { [updatedTransaction.id]: updatedTransaction } },
                                amountByCategory: { [updatedTransaction.category]: updatedTransaction.amount },
                                transactionsByDay: { [transactionDay]: { [updatedTransaction.id]: updatedTransaction } },
                                totalAmountSpent: updatedTransaction.type === "Expense" ? updatedTransaction.amount : 0
                            });
                        }

                        return batch.commit();
                    });
            }

        } else {
            //transaction is updated
            const oldTransactionDate = oldTransaction.date.toDate();
            const oldTransactionDay = oldTransactionDate.getDate();

            const newTransactionDate = updatedTransaction.date.toDate();
            const newTransactionDay = newTransactionDate.getDate();
            const oldMonthDocument = admin.firestore().collection("Wallets").doc(oldTransaction.walletId)
                .collection("Statistics").doc('' + oldTransactionDate.getFullYear())
                .collection("Months").doc(months[oldTransactionDate.getMonth()]);
            const newMonthDocument = admin.firestore().collection("Wallets").doc(updatedTransaction.walletId)
                .collection("Statistics").doc('' + newTransactionDate.getFullYear())
                .collection("Months").doc(months[newTransactionDate.getMonth()]);

            const updateOldMonth = {
                latestUpdateTime: firestore.Timestamp.now(),
                totalAmountSpent: admin.firestore.FieldValue.increment(oldTransaction.type === "Expense" ? -oldTransaction.amount : 0),
                [`amountByCategory.${oldTransaction.category}`]: admin.firestore.FieldValue.increment(-oldTransaction.amount),

                [`transactions.${oldTransaction.id}`]: admin.firestore.FieldValue.delete(),
                [`categories.${oldTransaction.category}.${oldTransaction.id}`]: admin.firestore.FieldValue.delete(),
                [`transactionsByDay.${oldTransactionDay}.${oldTransaction.id}`]: admin.firestore.FieldValue.delete()
            };


            const categoriesByAmountField = `amountByCategory.${updatedTransaction.category}`;
            const categoryIdField = `categories.${updatedTransaction.category}.${updatedTransaction.id}`;
            const dayIdField = `transactionsByDay.${newTransactionDay}.${updatedTransaction.id}`;
            const idField = `transactions.${updatedTransaction.id}`;

            const updateNewMonth = {
                latestUpdateTime: firestore.Timestamp.now(),
                totalAmountSpent: admin.firestore.FieldValue.increment(updatedTransaction.type === "Expense" ? updatedTransaction.amount : 0),
                [categoriesByAmountField]: admin.firestore.FieldValue.increment(updatedTransaction.amount),

                [idField]: updatedTransaction,
                [categoryIdField]: updatedTransaction,
                [dayIdField]: updatedTransaction
            };

            return Promise.all([oldMonthDocument.get(), newYearDocument.get(), newMonthDocument.get()]).then(result => {
                const oldMonthDoc = result[0];
                const newMonthDoc = result[1];

                if (oldMonthDoc.exists) {
                    batch.update(oldMonthDocument, updateOldMonth);
                }
                if (newMonthDoc.exists) {
                    batch.update(newMonthDocument, updateNewMonth);
                } else {
                    batch.set(newMonthDocument, {
                        latestUpdateTime: firestore.Timestamp.now(),
                        transactions: { [updatedTransaction.id]: updatedTransaction },
                        categories: { [updatedTransaction.category]: { [updatedTransaction.id]: updatedTransaction } },
                        amountByCategory: { [updatedTransaction.category]: updatedTransaction.amount },
                        transactionsByDay: { [newTransactionDay]: { [updatedTransaction.id]: updatedTransaction } },
                        totalAmountSpent: updatedTransaction.type === "Expense" ? updatedTransaction.amount : 0
                    });
                }

                return batch.commit();
            });
        }
    }
});