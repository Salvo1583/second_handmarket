package it.polito.mad.group18.second_handmarket.model
class CategoryList {
    private val list: LinkedHashMap<String, ArrayList<String>>


    fun getCategories() : Collection<String>{
        return list.keys
    }

    fun getSubCategories(c: String): Collection<String> {
        return list[c]!!
    }

    init {
        list = LinkedHashMap()
        var subCategories = ArrayList<String>()
        subCategories.add("Painting, Drawing & Art Supplies")
        subCategories.add("Sewing")
        subCategories.add("Scrapbooking & Stamping")
        subCategories.add("Party Decorations & Supplies")
        list["Arts & Crafts"] = subCategories
        subCategories = ArrayList()
        subCategories.add("Sports and Outdoors")
        subCategories.add("Outdoor Recreation")
        subCategories.add("Sports & Fitness")
        subCategories.add("Pet Supplies")
        list["Sports & Hobby"] = subCategories
        subCategories = ArrayList()
        subCategories.add("Apparel & Accessories")
        subCategories.add("Baby & Toddler Toys")
        subCategories.add("Car Seats & Accessories")
        subCategories.add("Pregnancy & Maternity")
        subCategories.add("Strollers & Accessories")
        list["Baby"] = subCategories
        subCategories = ArrayList()
        subCategories.add("Clothing")
        subCategories.add("Shoes")
        subCategories.add("Watches")
        subCategories.add("Handbags")
        subCategories.add("Accessories")
        list["Women's fashion"] = subCategories
        subCategories = ArrayList()
        subCategories.add("Clothing")
        subCategories.add("Shoes")
        subCategories.add("Watches")
        subCategories.add("Accessories")
        list["Men's fashion"] = subCategories
        subCategories=ArrayList()
        subCategories.add("Computers")
        subCategories.add("Monitors")
        subCategories.add("Printers & Scanners")
        subCategories.add("Camera & Photo")
        subCategories.add("Smartphone & Tablet")
        subCategories.add("Audio")
        subCategories.add("Television & Video")
        subCategories.add("Video Game Consoles")
        subCategories.add("Wearable Technology")
        subCategories.add("Accessories & Supplies")
        subCategories.add("Irons & Steamers")
        subCategories.add("Vacuums & Floor Care")
        list["Electronics"]=subCategories
        subCategories=ArrayList()
        subCategories.add("Action Figures & Statues")
        subCategories.add("Arts & Crafts")
        subCategories.add("Building Toys")
        subCategories.add("Dolls & Accessories")
        subCategories.add("Kids' Electronics")
        subCategories.add("Learning & Education")
        subCategories.add("Tricycles, Scooters & Wagons")
        subCategories.add("Videogames")
        list["Games & Videogames"]=subCategories
        subCategories= ArrayList()
        subCategories.add("Car Electronics & Accessories")
        subCategories.add("Accessories")
        subCategories.add("Motorcycle & Powersports")
        subCategories.add("Replacement Parts")
        subCategories.add("RV Parts & Accessories")
        subCategories.add("Tools & Equipment")
        list["Automotive"]=subCategories


    }
}