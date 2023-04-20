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

        //Transaction was deleted
        return yearDocument.get()
            .then((yearDocSnap) => {
                if (yearDocSnap.exists) {
                    // Update fields to remove transaction and amount           
                    return console.log("year doc exists");
                } else {
                    return console.log("year doc did not exist");
                }
            })
            .then(() => {
                monthDocument.get()
                    .then((monthDocSnap) => {
                        if (monthDocSnap.exists) {
                            // Update fields to remove transaction and amount      
                            return console.log("month doc exists");
                        } else {
                            return console.log("month doc did not exist");
                        }
                    });
            });
    } else {
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
            return yearDocument.get()
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
        } else {
            //transaction is updated
            const oldCategory = oldTransaction.category;
            const newCategory = updatedTransaction.category;
            const oldAmount = oldTransaction.amount;
            const newAmount = updatedTransaction.amount;
            const oldDate = oldTransaction.date.toDate();
            const newDate = updatedTransaction.date.toDate();

            const typeChanged = oldTransaction.type !== updatedTransaction.type;
            const categoryChanged = oldCategory !== newCategory;
            const amountChanged = oldAmount !== newAmount;
            const dateChanged = (oldDate.getFullYear() !== newDate.getFullYear()
                || oldDate.getMonth() !== newDate.getMonth()
                || oldDate.getDate() !== newDate.getDate());


            return console.log("Transaction was updated");
        }
    }


});


function createStatisticsDocument(doc, transaction, transactionDay, byDay) {
    const transactionToAdd = {
        id: transaction.id,
        amount: transaction.amount,
        title: transaction.title,
        currency: transaction.currency,
        category: transaction.category,
        date: transaction.date,
        dateLong: transaction.dateLong,
        picUrl: transaction.picUrl,
        type: transaction.type,
        userName: transaction.userName,
        user_id: transaction.user_id,
        walletId: transaction.walletId
    };
    if (byDay) {
        return doc.set({
            transactions: { [transaction.id]: transactionToAdd },
            categories: { [transaction.category]: { [transaction.id]: transactionToAdd } },
            amountByCategory: { [transaction.category]: transaction.amount },
            transactionsByDay: { [transactionDay]: { [transaction.id]: transactionToAdd } },
            totalAmountSpent: transaction.amount
        });
    } else {
        return doc.set({
            transactions: { [transaction.id]: transactionToAdd },
            categories: { [transaction.category]: { [transaction.id]: transactionToAdd } },
            amountByCategory: { [transaction.category]: transaction.amount },
            totalAmountSpent: transaction.amount
        });
    }

}

function updateStatisticsDocument(doc, transaction, transactionDay, byDay) {
    const { idField, titleField, amountField, currencyField, categoryField,
        dateField, dateLongField, picUrlField, typeField, userNameField,
        user_idField, walletIdField
    } = getTransactionsFields(transaction);

    const { dayIdField, dayTitleField, dayAmountField, dayCurrencyField,
        dayCategoryField, dayDateField, dayDateLongField, dayPicUrlField,
        dayTypeField, dayUserNameField, dayUser_idField, dayWalletIdField
    } = getDaysTransactionsFields(transactionDay, transaction);

    const { categoryIdField, categoryTitleField, categoryAmountField,
        categoryCurrencyField, categoryCategoryField, categoryDateField,
        categoryDateLongField, categoryPicUrlField, categoryTypeField,
        categoryUserNameField, categoryUser_idField, categoryWalletIdField
    } = getCategoriesTransactionsFields(transaction);

    const categoriesByAmountField = `amountByCategory.${transaction.category}`;

    if (byDay) {
        return doc.update({
            totalAmountSpent: admin.firestore.FieldValue.increment(transaction.amount),
            [categoriesByAmountField]: admin.firestore.FieldValue.increment(transaction.amount),

            [idField]: transaction.id,
            [titleField]: transaction.title,
            [amountField]: transaction.amount,
            [currencyField]: transaction.currency,
            [categoryField]: transaction.category,
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
            [categoryCategoryField]: transaction.category,
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
            [dayCategoryField]: transaction.category,
            [dayDateField]: transaction.date,
            [dayDateLongField]: transaction.dateLong,
            [dayPicUrlField]: transaction.picUrl,
            [dayTypeField]: transaction.type,
            [dayUserNameField]: transaction.userName,
            [dayUser_idField]: transaction.user_id,
            [dayWalletIdField]: transaction.walletId
        });
    } else {
        return doc.update({
            totalAmountSpent: admin.firestore.FieldValue.increment(transaction.amount),
            [categoriesByAmountField]: admin.firestore.FieldValue.increment(transaction.amount),

            [idField]: transaction.id,
            [titleField]: transaction.title,
            [amountField]: transaction.amount,
            [currencyField]: transaction.currency,
            [categoryField]: transaction.category,
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
            [categoryCategoryField]: transaction.category,
            [categoryDateField]: transaction.date,
            [categoryDateLongField]: transaction.dateLong,
            [categoryPicUrlField]: transaction.picUrl,
            [categoryTypeField]: transaction.type,
            [categoryUserNameField]: transaction.userName,
            [categoryUser_idField]: transaction.user_id,
            [categoryWalletIdField]: transaction.walletId
        });
    }

}
function getCategoriesTransactionsFields(transaction) {
    const categoryIdField = `categories.${transaction.category}.${transaction.id}.id`;
    const categoryTitleField = `categories.${transaction.category}.${transaction.id}.title`;
    const categoryAmountField = `categories.${transaction.category}.${transaction.id}.amount`;
    const categoryCurrencyField = `categories.${transaction.category}.${transaction.id}.currency`;
    const categoryCategoryField = `categories.${transaction.category}.${transaction.id}.category`;
    const categoryDateField = `categories.${transaction.category}.${transaction.id}.date`;
    const categoryDateLongField = `categories.${transaction.category}.${transaction.id}.dateLong`;
    const categoryPicUrlField = `categories.${transaction.category}.${transaction.id}.picUrl`;
    const categoryTypeField = `categories.${transaction.category}.${transaction.id}.type`;
    const categoryUserNameField = `categories.${transaction.category}.${transaction.id}.userName`;
    const categoryUser_idField = `categories.${transaction.category}.${transaction.id}.user_id`;
    const categoryWalletIdField = `categories.${transaction.category}.${transaction.id}.walletId`;
    return {
        categoryIdField, categoryTitleField, categoryAmountField, categoryCurrencyField, categoryCategoryField,
        categoryDateField, categoryDateLongField, categoryPicUrlField, categoryTypeField, categoryUserNameField,
        categoryUser_idField, categoryWalletIdField
    };
}

function getDaysTransactionsFields(transactionDay, transaction) {
    const dayIdField = `transactionsByDay.${transactionDay}.${transaction.id}.id`;
    const dayTitleField = `transactionsByDay.${transactionDay}.${transaction.id}.title`;
    const dayAmountField = `transactionsByDay.${transactionDay}.${transaction.id}.amount`;
    const dayCurrencyField = `transactionsByDay.${transactionDay}.${transaction.id}.currency`;
    const dayCategoryField = `transactionsByDay.${transactionDay}.${transaction.id}.category`;
    const dayDateField = `transactionsByDay.${transactionDay}.${transaction.id}.date`;
    const dayDateLongField = `transactionsByDay.${transactionDay}.${transaction.id}.dateLong`;
    const dayPicUrlField = `transactionsByDay.${transactionDay}.${transaction.id}.picUrl`;
    const dayTypeField = `transactionsByDay.${transactionDay}.${transaction.id}.type`;
    const dayUserNameField = `transactionsByDay.${transactionDay}.${transaction.id}.userName`;
    const dayUser_idField = `transactionsByDay.${transactionDay}.${transaction.id}.user_id`;
    const dayWalletIdField = `transactionsByDay.${transactionDay}.${transaction.id}.walletId`;
    return {
        dayIdField, dayTitleField, dayAmountField, dayCurrencyField, dayCategoryField, dayDateField,
        dayDateLongField, dayPicUrlField, dayTypeField, dayUserNameField, dayUser_idField, dayWalletIdField
    };
}

function getTransactionsFields(transaction) {
    const idField = `transactions.${transaction.id}.id`;
    const titleField = `transactions.${transaction.id}.title`;
    const amountField = `transactions.${transaction.id}.amount`;
    const currencyField = `transactions.${transaction.id}.currency`;
    const categoryField = `transactions.${transaction.id}.category`;
    const dateField = `transactions.${transaction.id}.date`;
    const dateLongField = `transactions.${transaction.id}.dateLong`;
    const picUrlField = `transactions.${transaction.id}.picUrl`;
    const typeField = `transactions.${transaction.id}.type`;
    const userNameField = `transactions.${transaction.id}.userName`;
    const user_idField = `transactions.${transaction.id}.user_id`;
    const walletIdField = `transactions.${transaction.id}.walletId`;
    return {
        idField, titleField, amountField, currencyField, categoryField, dateField, dateLongField,
        picUrlField, typeField, userNameField, user_idField, walletIdField
    };
}

