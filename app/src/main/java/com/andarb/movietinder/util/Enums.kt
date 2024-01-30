package com.andarb.movietinder.util


/** Identifies the type of button clicked on a list item */
enum class ClickType {
    LIKE, DELETE
}

/** Identifies how to filter movies */
enum class FilterMovies(val index: Int) {
    ALL(0), LIKE(1), DISLIKE(2)
}
