package com.example.mmdb.ui.personal.customlists.addtolists

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mmdb.model.data.CustomMovieList
import com.example.mmdb.model.data.Movie
import kotlinx.android.synthetic.main.popup_window_personal_lists_to_add.view.*

class AddToListsPopupWindow(
    private val root: View,
    private val view: View,
    width: Int, height: Int,
    private val selectedMovie: Movie
) : PopupWindow(view, width, height, true),
    AddToListsAdapter.ListCheckedListener {

    private var listsConfirmedClickListener: ListsConfirmedClickListener? = null

    private val checkedLists = mutableListOf<CustomMovieList>()

    init {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        showAtLocation(view, Gravity.CENTER, 0, 0)

        view.popup_window_outside.setOnClickListener { dismiss() }
        view.confirm_btn.setOnClickListener {
            listsConfirmedClickListener?.let {
                if (it.onConfirmListsClicked(selectedMovie, checkedLists, root))
                    dismiss()
            }
        }

        setupRecyclerView()
    }

    interface ListsConfirmedClickListener {
        fun onConfirmListsClicked(movie: Movie, checkedLists: List<CustomMovieList>, root: View): Boolean
    }

    fun setListsConfirmedClickListener(listener: ListsConfirmedClickListener) {
        listsConfirmedClickListener = listener
    }

    private fun setupRecyclerView(){
        view.personal_lists_recycler_view.layoutManager = LinearLayoutManager(root.context)
        view.personal_lists_recycler_view.addItemDecoration(
            DividerItemDecoration(
                root.context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    fun setupLists(movieLists: List<CustomMovieList>) {
        view.personal_lists_recycler_view.adapter = AddToListsAdapter(this, movieLists)
    }

    override fun onListChecked(movieList: CustomMovieList, isChecked: Boolean) {
        if (isChecked) checkedLists.add(movieList)
        else checkedLists.remove(movieList)
    }
}