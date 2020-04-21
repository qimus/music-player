package ru.den.musicplayer.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.den.musicplayer.models.*
import ru.den.musicplayer.searcher.AlbumSearcher
import ru.den.musicplayer.searcher.ArtistSearcher
import ru.den.musicplayer.searcher.TrackSearcher
import ru.den.musicplayer.searcher.YearSearcher
import ru.den.musicplayer.ui.viewmodel.ListViewModel
import ru.den.musicplayer.ui.viewmodel.TracksViewModel

val appModule = module {
    single {
        PlaylistTypeManager().apply {
            addPlaylistType(AllTracksPlaylistType())
            addPlaylistType(AlbumPlaylistType())
            addPlaylistType(ArtistPlaylistType())
            addPlaylistType(YearPlaylistType())
        }
    }

    single { Playlist() }

    single { TrackSearcher(androidContext()) }
    single { AlbumSearcher(androidContext()) }
    single { ArtistSearcher(androidContext()) }
    single { YearSearcher(androidContext()) }

    viewModel { TracksViewModel(get()) }
    viewModel(named("album")) { ListViewModel<Album, Unit, AlbumSearcher>(get()) }
    viewModel(named("artist")) { ListViewModel<Artist, Unit, ArtistSearcher>(get()) }
    viewModel(named("year")) { ListViewModel<Year, Unit, YearSearcher>(get()) }
}
