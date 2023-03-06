package com.example.chatappandroid.model
class User(
    var uid: String?,
    var name: String?,
    var phoneNumber: String?,
    var profileImage: String?,
) {
    // Add a no-argument constructor
    constructor() : this(null, null, null, null)
}