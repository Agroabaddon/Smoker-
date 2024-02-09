import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var textViewTemperature: TextView
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var inputStream: InputStream
    private lateinit var handler: Handler
    private var connected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewTemperature = findViewById(R.id.textViewTemperature)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        handler = Handler()

        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled, prompt the user to enable it
            // This can be done using Intent to the Bluetooth settings
            // or requesting user permission
        }

        // Connect to ESP32 device (Replace "ESP32 Device Name" with your ESP32 device name)
        connectToDevice("ESP32 Thermometer")
    }

    private fun connectToDevice(deviceName: String) {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        if (!pairedDevices.isNullOrEmpty()) {
            for (device in pairedDevices) {
                if (device.name == deviceName) {
                    val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for Serial Port Profile (SPP)
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                    bluetoothSocket.connect()
                    inputStream = bluetoothSocket.inputStream
                    connected = true
                    listenForData()
                    break
                }
            }
        }
    }

    private fun listenForData() {
        Thread {
            while (connected) {
                try {
                    val buffer = ByteArray(1024)
                    val bytesRead: Int = inputStream.read(buffer)
                    val tempString = String(buffer, 0, bytesRead)
                    handler.post {
                        // Update UI with temperature data
                        textViewTemperature.text = tempString
                    }
                } catch (e: IOException) {
                    Log.e("Bluetooth", "Error reading from input stream", e)
                    connected = false
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        connected = false
        bluetoothSocket.close()
    }
}
