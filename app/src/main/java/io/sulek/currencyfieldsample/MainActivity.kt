package io.sulek.currencyfieldsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        twoDollarsBtn.setOnClickListener { usdField.setDoubleValue(2.0, forceFractionDigits = false) }
        fiveDollarsBtn.setOnClickListener { usdField.setDoubleValue(5.0) }
        tenDollarsBtn.setOnClickListener { usdField.setDoubleValue(10.0) }
    }

}