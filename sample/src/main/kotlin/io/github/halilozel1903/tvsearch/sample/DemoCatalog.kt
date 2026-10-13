package io.github.halilozel1903.tvsearch.sample

import io.github.halilozel1903.tvsearch.core.SearchMatcher
import io.github.halilozel1903.tvsearch.core.Suggestions

/** One title of the demo catalog. */
data class DemoTitle(
    val id: String,
    val title: String,
    val year: Int,
    val genre: String,
    val format: String,
    val synopsis: String,
)

/**
 * A small catalog bundled with the sample so it works offline and the screenshots are stable.
 * Every title, synopsis and year is made up for this demo; none of it describes a real show or film.
 */
object DemoCatalog {
    val titles: List<DemoTitle> = listOf(
        DemoTitle("lightning-relay", "Lightning Relay", 2025, "Sports drama", "Film · 1h 46m", "A night school track team trains for one last relay before the stadium closes."),
        DemoTitle("light-years-apart", "Light Years Apart", 2025, "Science fiction", "Film · 1h 58m", "Two engineers on distant stations keep a long distance friendship alive with delayed messages."),
        DemoTitle("lighthouse-keepers", "Lighthouse Keepers", 2024, "Drama", "Series · 2 seasons", "Three siblings inherit a remote lighthouse and the guesthouse that comes with it."),
        DemoTitle("harbor-lights", "Harbor Lights", 2021, "Romance", "Film · 1h 39m", "A ferry captain and a lantern maker keep missing each other at the same harbor festival."),
        DemoTitle("the-last-lighthouse", "The Last Lighthouse", 2023, "Mystery", "Film · 2h 4m", "A keeper's logbook points to a ship that never reached the shore."),
        DemoTitle("northern-lights-diary", "Northern Lights Diary", 2026, "Documentary", "Series · 6 episodes", "A video diary of one winter spent chasing auroras across the far north."),
        DemoTitle("moonlight-bakery", "Moonlight Bakery", 2022, "Comedy", "Series · 3 seasons", "A bakery that only opens after midnight and the regulars who keep it going."),
        DemoTitle("paper-comets", "Paper Comets", 2024, "Adventure", "Film · 1h 52m", "Two kids build a paper observatory and follow a comet across the countryside."),
        DemoTitle("the-glass-harbor", "The Glass Harbor", 2025, "Thriller", "Series · 1 season", "A harbor town's glassworks hides a secret older than the town itself."),
        DemoTitle("quiet-orbit", "Quiet Orbit", 2026, "Science fiction", "Film · 2h 8m", "The last crew member of a research station listens for a signal from home."),
        DemoTitle("midnight-orchard", "Midnight Orchard", 2023, "Fantasy", "Film · 1h 44m", "Apples picked after midnight grant one small wish, and the town is running out of trees."),
        DemoTitle("copper-valley", "Copper Valley", 2022, "Western", "Series · 2 seasons", "A mining valley elects its first sheriff in thirty years."),
        DemoTitle("lantern-road", "Lantern Road", 2024, "Family", "Film · 1h 31m", "A family road trip follows a trail of paper lanterns to a grandmother's village."),
        DemoTitle("salt-and-ember", "Salt and Ember", 2021, "Drama", "Film · 1h 55m", "Two rival chefs share one tiny seaside kitchen for a summer."),
        DemoTitle("the-velvet-signal", "The Velvet Signal", 2025, "Thriller", "Series · 1 season", "A late night radio host starts receiving calls from tomorrow."),
        DemoTitle("tidewater", "Tidewater", 2020, "Mystery", "Film · 1h 49m", "Every low tide uncovers another clue on the same stretch of beach."),
        DemoTitle("seven-small-rooms", "Seven Small Rooms", 2023, "Comedy", "Series · 2 seasons", "Seven strangers share one very small apartment building."),
        DemoTitle("iron-meadow", "Iron Meadow", 2022, "Western", "Film · 2h 1m", "A blacksmith defends her meadow from a railway company."),
        DemoTitle("hollow-bay", "Hollow Bay", 2024, "Mystery", "Series · 2 seasons", "A podcast host returns to her hometown to reopen an old case."),
        DemoTitle("field-notes-from-mars", "Field Notes from Mars", 2026, "Documentary", "Series · 8 episodes", "Daily life in a simulated Mars habitat, told by its six volunteers."),
        DemoTitle("silver-static", "Silver Static", 2021, "Science fiction", "Film · 1h 37m", "An old television starts showing broadcasts from a city that does not exist."),
        DemoTitle("paper-mountains", "Paper Mountains", 2025, "Animation", "Series · 1 season", "A folded paper fox explores mountains made of old maps."),
        DemoTitle("the-long-ascent", "The Long Ascent", 2020, "Adventure", "Film · 2h 12m", "A climbing team attempts a route nobody has finished in forty years."),
        DemoTitle("night-ferry", "Night Ferry", 2023, "Thriller", "Film · 1h 41m", "One crossing, eleven passengers and a missing suitcase."),
    )

    /** What the sample starts with in "Recent searches". */
    val recentSearches: List<String> = listOf("northern lights", "paper comets", "mystery", "harbor", "quiet orbit")

    private val genres: List<String> = titles.map { it.genre }.distinct()

    /** Titles matching [query], title matches before genre matches. */
    fun search(query: String): List<DemoTitle> =
        SearchMatcher.filterByFields(query, titles) { listOf(it.title, it.genre) }

    /** Completions from recent searches, titles and genres. */
    fun suggestions(query: String, recent: List<String>): List<String> =
        if (query.isBlank()) emptyList() else Suggestions.rank(query, titles.map { it.title } + genres, recent, limit = 6)

    fun meta(title: DemoTitle): String = "${title.year} · ${title.genre} · ${title.format}"
}
