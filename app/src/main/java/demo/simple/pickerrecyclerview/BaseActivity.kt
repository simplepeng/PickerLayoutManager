package demo.simple.pickerrecyclerview

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {


    fun toast(text:String?){
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
    }
}