package it.polito.mad.group18.second_handmarket.model

data class User(var userId: String = "",
                var fullName: String = "",
                var nickName: String = "",
                var email: String = "",
                var phoneNumber: String = "",
                var geoArea: String = "",
                var totalRating: Double = 0.0,
                var numRatings: Int = 0,
                var pathPhoto: String = "",
                var loaded: Boolean = false,
                var longitude : Double = 0.0,
                var latitude : Double = 0.0)

