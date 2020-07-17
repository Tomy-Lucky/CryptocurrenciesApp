package com.arcadegamepark.cryptoapp.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.arcadegamepark.cryptoapp.api.ApiFactory
import com.arcadegamepark.cryptoapp.pojo.CoinPriceInfo
import com.arcadegamepark.cryptoapp.pojo.CoinPriceInfoRawData
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CoinViewModel(application: Application): AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    val priceList = db.coinPriceInfoDao().getPriceList()

    init { // This part of code will automatically operate when we create Object of this Class
        loadData()
    }

    private fun loadData() {
        val disposable = ApiFactory.apiService.getTopCoinsInfo()
            .map { it.data?.map { it.coinInfo?.name }?.joinToString(",") } // We get data Object then we coinInfo Object
                // After this we get all CoinInfo elements, convert it to 1 raw String and separate with comma(,)
            .flatMap { ApiFactory.apiService.getFullPriceList(fSyms = it) } // When we get necessary Coins List in String format
                // we just give it as parameter to download Detail Information about each Coin
            .map { getPriceListFromRawData(it) } // We just call method which convert JsonObject to necessary List of CoinPriceInfo Object
            .delaySubscription(10, TimeUnit.SECONDS) // Time Unit for repeating Load of Data
            .repeat() // Stop working when Internet is lost
            .retry() // Retry if Internet Connection is lost
            .subscribeOn(Schedulers.io()) // We do it in parallel Thread
            .subscribe({
                db.coinPriceInfoDao().insertCoinPriceInfo(it) // We add downloaded data to Database
                Log.d("TEST_OF_LOADING_DATA", "Success of Loading: $it")
            },{
                Log.d("TEST_OF_LOADING_DATA", "Fail of Loading: ${it.message}")
            })
        compositeDisposable.add(disposable)
    }

    private fun getPriceListFromRawData(coinPriceInfoRawData: CoinPriceInfoRawData):  List<CoinPriceInfo> {
        val result = ArrayList<CoinPriceInfo>()
        val jsonObject = coinPriceInfoRawData.coinPriceInfoJsonObject ?: return result // Elvis operator check if value is not Null
        val coinsNameList = jsonObject.keySet()
        for (coinName in coinsNameList) {
            val currencyJson = jsonObject.getAsJsonObject(coinName)
            val currencyNameList = currencyJson.keySet()
            for (currencyName in currencyNameList) {
                val coinPriceInfo = Gson().fromJson(currencyJson.getAsJsonObject(currencyName), CoinPriceInfo::class.java)
                result.add(coinPriceInfo)
            }
        }
        return result
    }

    fun getDetailInfo(fSym: String): LiveData<CoinPriceInfo> {
        return db.coinPriceInfoDao().getCoinPriceInfo(fSym)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}