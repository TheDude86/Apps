package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    MINE_TUNES_TITLE,
    HOME_BUTTON,
    MUSIC_BUTTON,
    SEARCH_BUTTON,
    SEARCH_TITLE,
    SEARCH_PLACEHOLDER,
    SEARCH_SONGS_BUTTON,
    SEARCH_ARTISTS_BUTTON,
    PLAYER_TITLE,
    PLAY_BUTTON,
    PAUSE_BUTTON,
    NEXT_TRACK_BUTTON,
    LAST_TRACK_BUTTON,
    LOOP_BUTTON,
    SHUFFLE_BUTTON,
    LIBRARY_TITLE,

    ARTIST_TITLE,
    ARTIST_STATS_PLACEHOLDER,
    ARTIST_PLAYS_PLACEHOLDER,
    ARTIST_SEE_ALL_SONGS_BUTTON,
    ARTIST_POPULAR_SONGS_TITLE,
    ARTIST_ALBUMS_TITLE,
    ;

    fun resource(): StringResource = StringResource("mine tunes", this.name)
}