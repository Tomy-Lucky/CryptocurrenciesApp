package com.arcadegamepark.cryptoapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arcadegamepark.cryptoapp.database.CoinViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_coin_detail.*

class CoinDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: CoinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_detail)

        if (!intent.hasExtra(EXTRA_FROM_SYMBOL)) {
            finish()
            return
        }
        val fromSymbol = intent.getStringExtra(EXTRA_FROM_SYMBOL)
        viewModel =
            ViewModelProvider(this).get(CoinViewModel::class.java) // Same as ViewModelProviders.of()
        viewModel.getDetailInfo(fromSymbol).observe(this, Observer {
            with(it) {
                tvPrice.text = price.toString()
                tvMinPrice.text = lowDay.toString()
                tvMaxPrice.text = highDay.toString()
                tvLastMarket.text = lastMarket
                tvLastUpdate.text = getFormattedTime()
                tvFromSymbol.text = fromSymbol
                tvToSymbol.text = toSymbol
                Picasso.get().load(getFullImageUrl()).into(ivLogoCoin)
            }
        })
    }

    companion object {
        private const val EXTRA_FROM_SYMBOL = "fSym"

        // Private and Comfortable way to create new Intent
        fun newIntent(context: Context, fromSymbol: String): Intent {
            val intent = Intent(context, CoinDetailActivity::class.java)
            intent.putExtra(EXTRA_FROM_SYMBOL, fromSymbol)
            return intent
        }
    }
}