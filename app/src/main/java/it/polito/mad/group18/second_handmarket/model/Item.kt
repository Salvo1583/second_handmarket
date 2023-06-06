package it.polito.mad.group18.second_handmarket.model

import com.google.android.libraries.places.api.model.Place

data class Rating(var value: Double = 0.0,
                  var description: String = "")

data class Item(var itemId: String="",
                var userId: String="",
                var title: String="",
                var category: String="",
                var subCategory: String ="",
                var description: String="",
                var price: String = "",
                var expireDate: String="",
                var location: String = "",
                var status: String ="",
                var buyerUserId: String = "",
                var rating: Rating? = null,
                var pathImage: String ="",
                var loaded: Boolean = false,
                var longitude : Double = 0.0,
                var latitude : Double = 0.0)