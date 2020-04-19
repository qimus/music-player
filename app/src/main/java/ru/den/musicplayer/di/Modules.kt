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
import ru.den.musicplayer.utils.Playlist

val appModule = module {
    single { Playlist(androidContext()) }
    single {
        PlaylistManager().apply {
            addPlaylist(AllTracksPlaylist())
            addPlaylist(AlbumPlaylist())
            addPlaylist(ArtistPlaylist())
            addPlaylist(YearPlaylist())
        }
    }

    single { TrackSearcher(androidContext()) }
    single { AlbumSearcher(androidContext()) }
    single { ArtistSearcher(androidContext()) }
    single { YearSearcher(androidContext()) }

    viewModel { TracksViewModel(get()) }
    viewModel(named("album")) { ListViewModel<Album, Unit, AlbumSearcher>(get()) }
    viewModel(named("artist")) { ListViewModel<Artist, Unit, ArtistSearcher>(get()) }
    viewModel(named("year")) { ListViewModel<Year, Unit, YearSearcher>(get()) }
}
