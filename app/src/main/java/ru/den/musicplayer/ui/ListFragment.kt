package ru.den.musicplayer.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_list.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

import ru.den.musicplayer.R
import ru.den.musicplayer.models.SoundEntity
import ru.den.musicplayer.searcher.MusicSearchCriteria
import ru.den.musicplayer.searcher.Searcher
import ru.den.musicplayer.ui.adapters.Contract
import ru.den.musicplayer.ui.viewmodel.ListViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment<M : SoundEntity, S: Searcher<Unit?, List<M>>>(
    private val adapter: Contract.Adapter<M>,
    viewModelName: String
) : Fragment() {

    companion object {
        private const val TAG = "ListFragment"

        @JvmStatic
        fun <M : SoundEntity, S: Searcher<Unit?, List<M>>> newInstance(adapter: Contract.Adapter<M>, viewModelName: String) =
            ListFragment<M, S>(adapter, viewModelName)
    }

    private val viewModel: ListViewModel<M, Unit?, S> by viewModel(named(viewModelName))

    private val onSelectListener = object : Contract.OnItemSelectContract<M> {
        override fun onSelect(model: M) {
            val musicSearchCriteria = MusicSearchCriteria.createFilterByModel(model)
            val fragment = TracksFragment.newInstance(musicSearchCriteria, viewModelName + model.id)

            activity?.supportFragmentManager?.let { fm ->
                fm.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter.setOnItemSelectListener(onSelectListener)
        configureObservable()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter as RecyclerView.Adapter<*>
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun configureObservable() {
        viewModel.getItems().observe(this, Observer { items ->
            adapter.updateItems(items)
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.search(null)
    }
}
