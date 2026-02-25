/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccLocation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import autodagger.AutoInjector
import com.gcc.talk.R
import com.gcc.talk.gccActivities.GccBaseActivity
import com.gcc.talk.gccAdapters.GccGeocodingAdapter
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.databinding.ActivityGeocodingBinding
import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import com.gcc.talk.gccViewmodels.GccGeoCodingViewModel
import fr.dudie.nominatim.client.TalkJsonNominatimClient
import fr.dudie.nominatim.model.Address
import okhttp3.OkHttpClient
import org.osmdroid.config.Configuration
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccGeocodingActivity : GccBaseActivity() {

    private lateinit var binding: ActivityGeocodingBinding

    @Inject
    lateinit var ncApi: GccNcApi

    @Inject
    lateinit var okHttpClient: OkHttpClient

    lateinit var roomToken: String
    private var chatApiVersion: Int = 1
    private var nominatimClient: TalkJsonNominatimClient? = null

    private var searchItem: MenuItem? = null
    var searchView: SearchView? = null

    lateinit var adapter: GccGeocodingAdapter
    private var geocodingResults: List<Address> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: GccGeoCodingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)

        binding = ActivityGeocodingBinding.inflate(layoutInflater)
        setupActionBar()
        setContentView(binding.root)
        initSystemBars()

        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

        roomToken = intent.getStringExtra(GccBundleKeys.KEY_ROOM_TOKEN)!!
        chatApiVersion = intent.getIntExtra(GccBundleKeys.KEY_CHAT_API_VERSION, 1)

        recyclerView = findViewById(R.id.geocoding_results)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GccGeocodingAdapter(this, geocodingResults)
        recyclerView.adapter = adapter
        viewModel = ViewModelProvider(this)[GccGeoCodingViewModel::class.java]

        var query = viewModel.getQuery()
        if (query.isEmpty() && intent.hasExtra(GccBundleKeys.KEY_GEOCODING_QUERY)) {
            query = intent.getStringExtra(GccBundleKeys.KEY_GEOCODING_QUERY).orEmpty()
            viewModel.setQuery(query)
        }
        val savedResults = viewModel.getGeocodingResults()
        initAdapter(savedResults)
        viewModel.getGeocodingResultsLiveData().observe(this) { results ->
            geocodingResults = results
            adapter.updateData(results)
        }
        val baseUrl = getString(R.string.osm_geocoder_url)
        val email = context.getString(R.string.osm_geocoder_contact)
        nominatimClient = TalkJsonNominatimClient(baseUrl, okHttpClient, email)
    }

    override fun onStart() {
        super.onStart()
        initAdapter(geocodingResults)
        initGeocoder()
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.getQuery().isNotEmpty() && adapter.itemCount == 0) {
            viewModel.searchLocation()
        } else {
            Log.e(TAG, "search string that was passed to GccGeocodingActivity was null or empty")
        }
        adapter.setOnItemClickListener(object : GccGeocodingAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val address: Address = adapter.getItem(position) as Address
                val geocodingResult = GccGeocodingResult(address.latitude, address.longitude, address.displayName)
                val intent = Intent(this@GccGeocodingActivity, GccLocationPickerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(GccBundleKeys.KEY_ROOM_TOKEN, roomToken)
                intent.putExtra(GccBundleKeys.KEY_CHAT_API_VERSION, chatApiVersion)
                intent.putExtra(GccBundleKeys.KEY_GEOCODING_RESULT, geocodingResult)
                startActivity(intent)
            }
        })
        searchView?.setQuery(viewModel.getQuery(), false)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.geocodingToolbar)
        binding.geocodingToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(resources!!.getColor(R.color.transparent, null).toDrawable())
        supportActionBar?.title = ""
        viewThemeUtils.material.themeToolbar(binding.geocodingToolbar)
    }

    private fun initAdapter(addresses: List<Address>) {
        adapter = GccGeocodingAdapter(binding.geocodingResults.context!!, addresses)
        adapter.setOnItemClickListener(object : GccGeocodingAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val address: Address = adapter.getItem(position) as Address
                val geocodingResult = GccGeocodingResult(address.latitude, address.longitude, address.displayName)
                val intent = Intent(this@GccGeocodingActivity, GccLocationPickerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(GccBundleKeys.KEY_ROOM_TOKEN, roomToken)
                intent.putExtra(GccBundleKeys.KEY_CHAT_API_VERSION, chatApiVersion)
                intent.putExtra(GccBundleKeys.KEY_GEOCODING_RESULT, geocodingResult)
                startActivity(intent)
            }
        })
        binding.geocodingResults.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_geocoding, menu)
        searchItem = menu.findItem(R.id.geocoding_action_search)
        initSearchView()
        searchItem?.expandActionView()
        searchView?.setQuery(viewModel.getQuery(), false)
        searchView?.clearFocus()
        return true
    }

    private fun initSearchView() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (searchItem != null) {
            searchView = searchItem!!.actionView as SearchView?

            searchView?.maxWidth = Int.MAX_VALUE
            searchView?.inputType = InputType.TYPE_TEXT_VARIATION_FILTER
            var imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN
            if (appPreferences.isKeyboardIncognito) {
                imeOptions = imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            }
            searchView?.imeOptions = imeOptions
            searchView?.queryHint = resources!!.getString(R.string.nc_search)
            searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.setQuery(query)
                    viewModel.searchLocation()
                    searchView?.clearFocus()
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    // This is a workaround to not set viewModel data when onQueryTextChange is triggered on startup
                    // Otherwise it would be set to an empty string.
                    if (searchView?.width!! > 0) {
                        viewModel.setQuery(query)
                    }
                    return true
                }
            })

            searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean = true

                override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                    val intent = Intent(context, GccLocationPickerActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(GccBundleKeys.KEY_ROOM_TOKEN, roomToken)
                    intent.putExtra(GccBundleKeys.KEY_CHAT_API_VERSION, chatApiVersion)
                    startActivity(intent)
                    return true
                }
            })
        }
    }

    private fun initGeocoder() {
        val baseUrl = getString(R.string.osm_geocoder_url)
        val email = context.getString(R.string.osm_geocoder_contact)
        nominatimClient = TalkJsonNominatimClient(baseUrl, okHttpClient, email)
    }

    companion object {
        val TAG = GccGeocodingActivity::class.java.simpleName
    }
}
