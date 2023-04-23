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

exports.testTransactionListener = functions.firestore.document("TestTransactions/{transactionID}").onWrite((snap, context) => {
    const updatedTransaction = snap.after.exists ? snap.after.data() : null;
    const oldTransaction = snap.before.exists ? snap.before.data() : null;


    if (updatedTransaction == null) {
        //Transaction was deleted
        const transactionDate = oldTransaction.date.toDate();
        const transactionDay = transactionDate.getDate();

        const yearDocument = admin.firestore()
            .collection("Wallets")
            .doc(oldTransaction.walletId)
            .collection("Statistics")
            .doc('' + transactionDate.getFullYear());
        const monthDocument = admin.firestore()
            .collection("Wallets")
            .doc(oldTransaction.walletId)
            .collection("Statistics")
            .doc('' + transactionDate.getFullYear())
            .collection("Months")
            .doc(months[transactionDate.getMonth()]);

        //Update wallet balance
        return admin.firestore().collection("Wallets").doc(oldTransaction.walletId).update({
            amount: admin.firestore.FieldValue.increment(oldTransaction.type === "Expense" ? oldTransaction.amount : -oldTransaction.amount)
        }).then(() => {
            yearDocument.get()
                .then((yearDocSnap) => {
                    if (yearDocSnap.exists) {
                        // Update fields to remove transaction and amount                    
                        return removeTransactionFromStatistics(yearDocument, oldTransaction, transactionDay, false);

                    } else {
                        return console.log("year doc did not exist");
                    }
                })
                .then(() => {
                    monthDocument.get()
                        .then((monthDocSnap) => {
                            if (monthDocSnap.exists) {
                                // Update fields to remove transaction and amount      
                                return removeTransactionFromStatistics(monthDocument, oldTransaction, transactionDay, true);
                            } else {
                                return console.log("month doc did not exist");
                            }
                        });
                });
        });

    } else {
        //Transaction was either created or updated
        const transactionDate = updatedTransaction.date.toDate();
        const transactionDay = transactionDate.getDate();

        const yearDocument = admin.firestore()
            .collection("Wallets")
            .doc(updatedTransaction.walletId)
            .collection("Statistics")
            .doc('' + transactionDate.getFullYear());
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
            return admin.firestore().collection("Wallets").doc(updatedTransaction.walletId).update({
                amount: admin.firestore.FieldValue.increment(updatedTransaction.type === "Income" ? updatedTransaction.amount : -updatedTransaction.amount)
            }).then(() => {
                yearDocument.get()
                    .then((yearDocSnap) => {
                        if (yearDocSnap.exists) {
                            return updateStatisticsDocument(yearDocument, updatedTransaction, transactionDay, false);
                        } else {
                            return createStatisticsDocument(yearDocument, updatedTransaction, transactionDay, false);
                        }
                    })
                    .then(() => {
                        monthDocument.get()
                            .then((monthDocSnap) => {
                                if (monthDocSnap.exists) {
                                    return updateStatisticsDocument(monthDocument, updatedTransaction, transactionDay, true);
                                } else {
                                    return createStatisticsDocument(monthDocument, updatedTransaction, transactionDay, true);
                                }
                            });
                    });
            });
        } else {
            //transaction is updated
            const oldTransactionDate = oldTransaction.date.toDate();
            const oldTransactionDay = oldTransactionDate.getDate();

            const newTransactionDate = updatedTransaction.date.toDate();
            const newTransactionDay = newTransactionDate.getDate();

            const oldYearDocument = admin.firestore().collection("Wallets").doc(oldTransaction.walletId)
                .collection("Statistics").doc('' + oldTransactionDate.getFullYear());
            const oldMonthDocument = admin.firestore().collection("Wallets").doc(oldTransaction.walletId)
                .collection("Statistics").doc('' + oldTransactionDate.getFullYear())
                .collection("Months").doc(months[oldTransactionDate.getMonth()]);

            const newYearDocument = admin.firestore().collection("Wallets").doc(updatedTransaction.walletId)
                .collection("Statistics").doc('' + newTransactionDate.getFullYear());
            const newMonthDocument = admin.firestore().collection("Wallets").doc(updatedTransaction.walletId)
                .collection("Statistics").doc('' + newTransactionDate.getFullYear())
                .collection("Months").doc(months[newTransactionDate.getMonth()]);

            return updateWalletAmount(oldTransaction, updatedTransaction)
                .then(() => {
                    //Remove old transaction
                    return oldYearDocument.get()
                        .then((yearDocSnap) => {
                            if (yearDocSnap.exists) {
                                // Update fields to remove transaction and amount                    
                                return removeTransactionFromStatistics(oldYearDocument, oldTransaction, oldTransactionDay, false);

                            } else {
                                return console.log("year doc did not exist");
                            }
                        })
                        .then(() => {
                            oldMonthDocument.get()
                                .then((monthDocSnap) => {
                                    if (monthDocSnap.exists) {
                                        // Update fields to remove transaction and amount      
                                        return removeTransactionFromStatistics(oldMonthDocument, oldTransaction, oldTransactionDay, true);
                                    } else {
                                        return console.log("month doc did not exist");
                                    }
                                });
                        }).then(() => {
                            //Add new transaction
                            newYearDocument.get()
                                .then((newYearDocSnap) => {
                                    if (newYearDocSnap.exists) {
                                        return updateStatisticsDocument(newYearDocument, updatedTransaction, newTransactionDay, false);
                                    } else {
                                        return createStatisticsDocument(newYearDocument, updatedTransaction, newTransactionDay, false);
                                    }
                                })
                                .then(() => {
                                    newMonthDocument.get()
                                        .then((newMonthDocSnap) => {
                                            if (newMonthDocSnap.exists) {
                                                return updateStatisticsDocument(newMonthDocument, updatedTransaction, newTransactionDay, true);
                                            } else {
                                                return createStatisticsDocument(newMonthDocument, updatedTransaction, newTransactionDay, true);
                                            }
                                        });
                                });
                        });
                });
        }
    }
});

function updateWalletAmount(transBefore, transAfter) {
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

function removeTransactionFromStatistics(doc, oldTransaction, transactionDay, byDay) {
    const categoriesByAmountField = `amountByCategory.${oldTransaction.category}`;

    if (byDay) {
        return doc.update({
            totalAmountSpent: admin.firestore.FieldValue.increment(oldTransaction.type === "Expense" ? -oldTransaction.amount : 0),
            [categoriesByAmountField]: admin.firestore.FieldValue.increment(-oldTransaction.amount),

            [`transactions.${oldTransaction.id}`]: admin.firestore.FieldValue.delete(),
            [`categories.${oldTransaction.category}.${oldTransaction.id}`]: admin.firestore.FieldValue.delete(),
            [`transactionsByDay.${transactionDay}.${oldTransaction.id}`]: admin.firestore.FieldValue.delete()
        });
    } else {
        return doc.update({
            totalAmountSpent: admin.firestore.FieldValue.increment(oldTransaction.type === "Expense" ? -oldTransaction.amount : 0),
            [categoriesByAmountField]: admin.firestore.FieldValue.increment(-oldTransaction.amount),

            [`transactions.${oldTransaction.id}`]: admin.firestore.FieldValue.delete(),
            [`categories.${oldTransaction.category}.${oldTransaction.id}`]: admin.firestore.FieldValue.delete()
        });
    }
}

function createStatisticsDocument(doc, transaction, transactionDay, byDay) {
    if (byDay) {
        return doc.set({
            transactions: { [transaction.id]: transaction },
            categories: { [transaction.category]: { [transaction.id]: transaction } },
            amountByCategory: { [transaction.category]: transaction.amount },
            transactionsByDay: { [transactionDay]: { [transaction.id]: transaction } },
            totalAmountSpent: transaction.type === "Expense" ? transaction.amount : 0
        });
    } else {
        return doc.set({
            transactions: { [transaction.id]: transaction },
            categories: { [transaction.category]: { [transaction.id]: transaction } },
            amountByCategory: { [transaction.category]: transaction.amount },
            totalAmountSpent: transaction.type === "Expense" ? transaction.amount : 0
        });
    }

}

function updateStatisticsDocument(doc, transaction, transactionDay, byDay) {
    const categoriesByAmountField = `amountByCategory.${transaction.category}`;
    const categoryIdField = `categories.${transaction.category}.${transaction.id}`;
    const dayIdField = `transactionsByDay.${transactionDay}.${transaction.id}`;
    const idField = `transactions.${transaction.id}`;

    if (byDay) {
        return doc.update({
            totalAmountSpent: admin.firestore.FieldValue.increment(transaction.type === "Expense" ? transaction.amount : 0),
            [categoriesByAmountField]: admin.firestore.FieldValue.increment(transaction.amount),

            [idField]: transaction,
            [categoryIdField]: transaction,
            [dayIdField]: transaction
        });
    } else {
        return doc.update({
            totalAmountSpent: admin.firestore.FieldValue.increment(transaction.type === "Expense" ? transaction.amount : 0),
            [categoriesByAmountField]: admin.firestore.FieldValue.increment(transaction.amount),

            [idField]: transaction,
            [categoryIdField]: transaction
        });
    }
}