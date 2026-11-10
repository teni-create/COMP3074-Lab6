package ca.gbc.comp3074.teni.ogun.sensorlab

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.teni.ogun.sensorlab.ui.theme.SensorLabTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var temperatureSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var lightSensor: Sensor? = null

    // Compose-observable state
    private var temperature by mutableStateOf<Float?>(null)
    private var gyroX by mutableStateOf(0f)
    private var gyroY by mutableStateOf(0f)
    private var gyroZ by mutableStateOf(0f)
    private var temperatureAvailable by mutableStateOf(false)
    private var gyroscopeAvailable by mutableStateOf(false)

    private var lightLevel by mutableStateOf<Float?>(null)
    private var lightAvailable by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // obtain SensorManager and discover the sensors
        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        temperatureSensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_AMBIENT_TEMPERATURE
            )

        gyroscopeSensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_GYROSCOPE
            )

        lightSensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_LIGHT
            )

        temperatureAvailable = temperatureSensor != null
        gyroscopeAvailable = gyroscopeSensor != null
        lightAvailable = lightSensor != null

        setContent {
            SensorLabTheme {
                SensorScreen(
                    temperature = temperature,
                    temperatureAvailable = temperatureAvailable,
                    gyroX = gyroX,
                    gyroY = gyroY,
                    gyroZ = gyroZ,
                    gyroscopeAvailable = gyroscopeAvailable,
                    lightLevel = lightLevel,
                    lightAvailable = lightAvailable
                )
            }
        }
    }

    // register the sensor listeners
    override fun onResume() {
        super.onResume()

        temperatureSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        gyroscopeSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        lightSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    // unregister the listener (this stops ALL sensors, including light)
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // process sensor events
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                temperature = event.values[0]
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
            }

            Sensor.TYPE_LIGHT -> {
                lightLevel = event.values[0]
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // Not required for this lab.
    }
}

// main sensor screen
@Composable
fun SensorScreen(
    temperature: Float?,
    temperatureAvailable: Boolean,
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    gyroscopeAvailable: Boolean,
    lightLevel: Float?,
    lightAvailable: Boolean
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Android Sensor Monitor",
                style = MaterialTheme.typography.headlineMedium
            )

            TemperatureCard(
                temperature = temperature,
                available = temperatureAvailable
            )

            GyroscopeCard(
                x = gyroX,
                y = gyroY,
                z = gyroZ,
                available = gyroscopeAvailable
            )

            LightCard(
                lightLevel = lightLevel,
                available = lightAvailable
            )
        }
    }
}

// temperature card
@Composable
fun TemperatureCard(
    temperature: Float?,
    available: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ambient Temperature",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                !available ->
                    Text("Temperature sensor is not available.")

                temperature == null ->
                    Text("Waiting for sensor reading...")

                else -> Text(
                    text = "%.1f °C".format(temperature),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

// gyroscope card
@Composable
fun GyroscopeCard(
    x: Float,
    y: Float,
    z: Float,
    available: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Gyroscope",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!available) {
                Text("Gyroscope is not available.")
            } else {
                Text("X: %.3f rad/s".format(x))
                Text("Y: %.3f rad/s".format(y))
                Text("Z: %.3f rad/s".format(z))
            }
        }
    }
}

// Light sensor extension
@Composable
fun LightCard(
    lightLevel: Float?,
    available: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ambient Light",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                !available ->
                    Text("Light sensor is not available.")

                lightLevel == null ->
                    Text("Waiting for sensor reading...")

                else -> Text(
                    text = "%.1f lx".format(lightLevel),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

// Preview so you can see the layout in Android Studio
@Preview(showBackground = true)
@Composable
fun SensorScreenPreview() {
    SensorLabTheme {
        SensorScreen(
            temperature = null,
            temperatureAvailable = false,
            gyroX = 0.012f,
            gyroY = -0.004f,
            gyroZ = 0.001f,
            gyroscopeAvailable = true,
            lightLevel = 320f,
            lightAvailable = true
        )
    }
}
