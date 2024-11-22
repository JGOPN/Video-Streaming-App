package com.pdm.streamingapp.data

import com.pdm.streamingapp.model.Movie
import com.pdm.streamingapp.model.User
import java.time.LocalDate

object Datasource {
    val users = listOf(
        User(1, "Joao", "ffff", "jj@gmail.com", true, LocalDate.of(1985, 1, 1)),
        User(2, "Jeswus", "aaa", "jc@heavn.com", false, LocalDate.of(1992, 1, 1)),
        User(3, "Maria", "bbb", "maria@gmail.com", false, LocalDate.of(1990, 1, 1)),
        User(4, "Napoleao", "aaaa", "napo@leao.fr", true, LocalDate.of(1980, 1, 1)),
        User(5, "Alice", "ccc", "alice@wonderland.com", true, LocalDate.of(1988, 1, 1)),
        User(6, "Bob", "ddd", "bob@builder.com", false, LocalDate.of(1995, 1, 1)),
        User(7, "Carol", "eee", "carol@network.com", true, LocalDate.of(1987, 1, 1)),
        User(8, "Dave", "fff", "dave@tech.com", false, LocalDate.of(1993, 1, 1)),
        User(9, "Eve", "ggg", "eve@security.com", true, LocalDate.of(1991, 1, 1)),
        User(10, "Frank", "hhh", "frank@data.com", false, LocalDate.of(1989, 1, 1)),
        User(11, "Grace", "iii", "grace@compute.com", true, LocalDate.of(1986, 1, 1)),
        User(12, "Heidi", "jjj", "heidi@crypto.com", false, LocalDate.of(1994, 1, 1)),
        User(13, "Ivan", "kkk", "ivan@risk.com", true, LocalDate.of(1982, 1, 1)),
        User(14, "Judy", "lll", "judy@codes.com", false, LocalDate.of(1990, 1, 1)),
        User(15, "Mallory", "mmm", "mallory@attack.com", true, LocalDate.of(1984, 1, 1))
    )

    val movies = listOf(
        Movie(1, "Inception", "A mind-bending thriller", 2010, 1, 148, listOf("Sci-Fi", "Thriller")),
        Movie(2, "The Matrix", "A hacker discovers reality is an illusion", 1999, 2, 136, listOf("Sci-Fi", "Action")),
        Movie(3, "The Shawshank Redemption", "A man finds hope in prison", 1994, 3, 142, listOf("Drama")),
        Movie(4, "Pulp Fiction", "Stories of crime intertwine", 1994, 4, 154, listOf("Crime", "Drama")),
        Movie(5, "The Dark Knight", "Batman faces the Joker", 2008, 1, 152, listOf("Action", "Crime")),
        Movie(6, "Forrest Gump", "A man with a big heart lives an extraordinary life and aids and some filler herew to test the screen size yadda yadda", 1994, 2, 142, listOf("Drama", "Romance")),
        Movie(7, "Fight Club", "An office worker and a soap maker form a fight club", 1999, 3, 139, listOf("Drama")),
        Movie(8, "Interstellar", "A team travels through a wormhole", 2014, 4, 169, listOf("Sci-Fi", "Adventure")),
        Movie(9, "The Godfather", "The story of the Corleone family", 1972, 1, 175, listOf("Crime", "Drama")),
        Movie(10, "The Lord of the Rings: The Fellowship of the Ringssssssssssssssssssss", "A hobbit embarks on a journey", 2001, 2, 178, listOf("Fantasy", "Adventure")),
        Movie(11, "The Lion King", "A young lion cub finds his place", 1994, 3, 88, listOf("Animation", "Adventure")),
        Movie(12, "Gladiator", "A betrayed general seeks revenge", 2000, 4, 155, listOf("Action", "Drama")),
        Movie(13, "Jurassic Park", "Dinosaurs come back to life", 1993, 1, 127, listOf("Adventure", "Sci-Fi")),
        Movie(14, "Titanic", "A romance blossoms on a doomed ship", 1997, 2, 195, listOf("Drama", "Romance")),
        Movie(15, "Saving Private Ryan", "A mission to save one soldier", 1998, 3, 169, listOf("Action", "War"))
    )


    val genres = listOf(
        "Action","Animation","Comedy","Drama","Documentary","Horror","Sci-Fi","Romance","Thriller"
    )

}