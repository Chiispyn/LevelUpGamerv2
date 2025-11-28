package com.levelupgamer.levelup.data

import com.levelupgamer.levelup.R
import com.levelupgamer.levelup.model.Event
import com.levelupgamer.levelup.model.Product
import com.levelupgamer.levelup.model.Review
import com.levelupgamer.levelup.model.Reward
import com.levelupgamer.levelup.model.RewardType

object InitialData {

    fun getInitialProducts(): List<Product> {
        return listOf(
            Product(
                code = "JM001", category = "Juegos de Mesa", name = "Catan", price = 29990, quantity = 15,
                description = "El clásico juego de estrategia y gestión de recursos. ¡Coloniza la isla, comercia y construye tu camino a la victoria!", 
                imageResId = R.drawable.catan, imageUrl = null
            ),
            Product(
                code = "JM002", category = "Juegos de Mesa", name = "Carcassonne", price = 24990, quantity = 12,
                description = "Un adictivo juego de colocación de losetas donde creas un paisaje medieval, reclamando ciudades, caminos y granjas.", 
                imageResId = R.drawable.juegocarcassonne, imageUrl = null
            ),
            Product(
                code = "AC001", category = "Accesorios", name = "Control Inalámbrico Xbox Series X", price = 59990, quantity = 20,
                description = "Diseño modernizado, gatillos texturizados y un D-pad híbrido. La mejor herramienta para tus victorias en Xbox.", 
                imageResId = R.drawable.joystick, imageUrl = null
            ),
            Product(
                code = "AC002", category = "Accesorios", name = "Auriculares HyperX Cloud II", price = 79990, quantity = 8,
                description = "Sumérgete en el juego con sonido envolvente 7.1. Comodidad legendaria para maratones de gaming.", 
                imageResId = R.drawable.audifonos, imageUrl = null
            ),
            Product(
                code = "CO001", category = "Consolas", name = "PlayStation 5", price = 549990, quantity = 5,
                description = "Experimenta una velocidad sorprendente con su SSD de ultra alta velocidad, inmersión más profunda con soporte para retroalimentación háptica y gatillos adaptativos.", 
                imageResId = R.drawable.ps5pro, imageUrl = null
            ),
            Product(
                code = "CG001", category = "Computadores Gamers", name = "PC Gamer ASUS ROG Strix", price = 1299990, quantity = 3,
                description = "Potencia y estilo en un solo equipo. Equipado para correr los últimos títulos en ultra sin sudar.", 
                imageResId = R.drawable.pc, imageUrl = null
            ),
            Product(
                code = "SG001", category = "Sillas Gamers", name = "Silla Gamer Secretlab Titan", price = 349990, quantity = 7,
                description = "Ergonomía de élite para largas sesiones de juego. Tu espalda te lo agradecerá.", 
                imageResId = R.drawable.silla, imageUrl = null
            ),
            Product(
                code = "MS001", category = "Mouse", name = "Mouse Gamer Logitech G502 HERO", price = 49990, quantity = 25,
                description = "El mouse más popular del mundo, ahora con el sensor HERO 25K para una precisión y capacidad de respuesta inigualables.", 
                imageResId = R.drawable.mouse, imageUrl = null
            ),
            Product(
                code = "MP001", category = "Mousepad", name = "Mousepad Razer Goliathus Extended", price = 29990, quantity = 30,
                description = "Una superficie microtexturizada para un seguimiento preciso de píxeles, optimizada para todos los sensores de mouse.", 
                imageResId = R.drawable.mousepad, imageUrl = null
            ),
            Product(
                code = "PP001", category = "Poleras Personalizadas", name = "Polera Gamer Personalizada 'Level-Up'", price = 14990, quantity = 50,
                description = "Viste tu pasión. Polera de algodón de alta calidad con el logo exclusivo de Level-Up Gamer.", 
                imageResId = R.drawable.polera, imageUrl = null
            )
        )
    }

    fun getInitialReviews(): List<Review> {
        return listOf(
            Review(productCode = "CO001", userId = 1, userName = "GamerX", rating = 5, comment = "¡Una bestia de consola! Los tiempos de carga son cosa del pasado. 100% recomendada."),
            Review(productCode = "CO001", userId = 2, userName = "ConsoleroMaster", rating = 4, comment = "Muy buena, pero me gustaría que tuviera más almacenamiento base. Por lo demás, impecable."),
            Review(productCode = "AC002", userId = 3, userName = "AudiofiloGamer", rating = 5, comment = "Los mejores audífonos que he tenido. El 7.1 es increíble para shooters."),
            Review(productCode = "JM001", userId = 1, userName = "GamerX", rating = 5, comment = "Un clásico que nunca falla. Ideal para jugar con amigos y familia."),
            Review(productCode = "JM001", userId = 4, userName = "Estratega", rating = 4, comment = "Muy entretenido, aunque dependes mucho de la suerte de los dados a veces."),
            Review(productCode = "MS001", userId = 5, userName = "ProPlayer", rating = 5, comment = "El sensor es perfecto, no hay más que decir. El mejor mouse competitivo.")
            // El producto PP001 (Polera) se deja sin reviews intencionadamente
        )
    }

    fun getInitialRewards(): List<Reward> {
        return listOf(
            Reward("d-5000", "Descuento de $5.000", "Un descuento de $5.000 para tu próxima compra.", 5000, RewardType.DISCOUNT_AMOUNT.name, 5000.0),
            Reward("d-10p", "10% de Descuento", "Un 10% de descuento en el total de tu próxima compra.", 9000, RewardType.DISCOUNT_PERCENTAGE.name, 10.0, 100),
            Reward("s-free", "Envío Gratis", "Obtén envío gratis en tu próximo pedido a todo Chile.", 7500, RewardType.FREE_SHIPPING.name, stock = 50),
            Reward("p-polera", "Polera Exclusiva Level-Up", "Una polera de edición limitada para verdaderos gamers.", 15000, RewardType.FREE_PRODUCT.name, productCode = "PP001", stock = 25)
        )
    }

    fun getInitialEvents(): List<Event> {
        return listOf(
            Event("torneo-lol-1", "Torneo de League of Legends", "Compite por la gloria y grandes premios en nuestro torneo mensual de LoL.", "30-07-2024", "18:00", "Online", 100, 5000),
            Event("noche-juegos-1", "Noche de Juegos de Mesa", "Ven a nuestra tienda a disfrutar de una noche de juegos de mesa con la comunidad.", "15-08-2024", "19:00", "Tienda Level-Up", 50, 1000)
        )
    }
}
