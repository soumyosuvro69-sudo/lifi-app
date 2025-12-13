# lifi-app
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.first {
            cameraManager.getCameraCharacteristics(it)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        val editText = findViewById<EditText>(id.editTextWord)
        val button = findViewById<Button>(id=R.id.buttonSend)

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 50)
        }

        button.setOnClickListener {
            val word = editText.text.toString()
            sendWordViaFlash(word)
        }
    }

    private fun sendWordViaFlash(word: String) {
        Thread {
            for (char in word) {
                val binString = String.format("%8s", Integer.toBinaryString(char.code)).replace(' ', '0')
                for (bit in binString) {
                    if (bit == '1') {
                        setFlashlight(true)
                    } else {
                        setFlashlight(false)
                    }
                    Thread.sleep(300) // Match Arduino delay
                }
            }
            setFlashlight(false)
        }.start()
    }

    private fun setFlashlight(state: Boolean) {
        cameraManager.setTorchMode(cameraId, state)
    }
}
