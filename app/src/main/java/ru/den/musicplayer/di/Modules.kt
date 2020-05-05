package ru.den.musicplayer.di

import android.app.Application
import android.content.res.Resources
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.den.musicplayer.database.MusicDatabase
import ru.den.musicplayer.database.dao.PlaylistDao
import ru.den.musicplayer.database.dao.PlaylistItemDao
import ru.den.musicplayer.models.*
import ru.den.musicplayer.searcher.AlbumSearcher
import ru.den.musicplayer.searcher.ArtistSearcher
import ru.den.musicplayer.searcher.TrackSearcher
import ru.den.musicplayer.searcher.YearSearcher
import ru.den.musicplayer.ui.myplaylists.PlaylistsViewModel
import ru.den.musicplayer.ui.viewmodel.ListViewModel
import ru.den.musicplayer.ui.viewmodel.TracksViewModel

val appModule = module {
    fun provideResources(application: Application): Resources {
        return application.resources
    }

    single {
        PlaylistsManager().apply {
            addPlaylistType(AllTracksPlaylist())
            addPlaylistType(AlbumPlaylist())
            addPlaylistType(ArtistPlaylist())
            addPlaylistType(YearPlaylist())
            addPlaylistType(MyPlaylists(provideResources(get())))
        }
    }

    single { CurrentPlaylist() }

    single { TrackSearcher(androidContext()) }
    single { AlbumSearcher(androidContext()) }
    single { ArtistSearcher(androidContext()) }
    single { YearSearcher(androidContext()) }
}

val viewModelModule = module {
    viewModel { TracksViewModel(get()) }
    viewModel(named("album")) { ListViewModel<Album, Unit, AlbumSearcher>(get()) }
    viewModel(named("artist")) { ListViewModel<Artist, Unit, ArtistSearcher>(get()) }
    viewModel(named("year")) { ListViewModel<Year, Unit, YearSearcher>(get()) }

    viewModel { PlaylistsViewModel(get()) }
}

val databaseModule = module {
    fun provideDatabase(application: Application): MusicDatabase {
        return Room.databaseBuilder(application, MusicDatabase::class.java, "yamp.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun providePlaylistDao(database: MusicDatabase): PlaylistDao {
        return database.playlistDao
    }

    fun providePlaylistItemDao(database: MusicDatabase): PlaylistItemDao {
        return database.playlistItemDao
    }

    single { provideDatabase(androidApplication()) }
    single { providePlaylistDao(get()) }
    single { providePlaylistItemDao(get()) }
}
