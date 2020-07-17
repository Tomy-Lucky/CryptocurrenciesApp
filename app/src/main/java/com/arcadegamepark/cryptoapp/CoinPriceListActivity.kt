package com.arcadegamepark.cryptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arcadegamepark.cryptoapp.adapters.CoinInfoAdapter
import com.arcadegamepark.cryptoapp.database.CoinViewModel
import com.arcadegamepark.cryptoapp.pojo.CoinPriceInfo
import kotlinx.android.synthetic.main.activity_coin_price_list.*

class CoinPriceListActivity : AppCompatActivity() {

    private lateinit var viewModel: CoinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_price_list)

        viewModel =
            ViewModelProvider(this).get(CoinViewModel::class.java) // Same as ViewModelProviders.of()
        val adapter = CoinInfoAdapter(this)
        rvCoinPriceList.adapter = adapter

        viewModel.priceList.observe(this, Observer {
            adapter.coinInfoList = it
        })

        adapter.onCoinClickListener = object : CoinInfoAdapter.OnCoinClickListener {
            override fun onCoinClick(coinPriceInfo: CoinPriceInfo) {
                val intent = CoinDetailActivity.newIntent(this@CoinPriceListActivity, coinPriceInfo.fromSymbol)
                startActivity(intent)
            }

        }
//        viewModel.getDetailInfo("BTC").observe(this, Observer {
//            Log.d("TEST_OF_LOADING_DATA", "Success in activity: $it")
//        })

    }
}