package com.example.fitnessapp.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.fitnessapp.databinding.FragmentStatisticPageBinding
import com.example.fitnessapp.domain.User
import com.example.fitnessapp.service.StepCounterService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class StatisticPageFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentStatisticPageBinding? = null
    private val binding: FragmentStatisticPageBinding
        get() = _binding ?: throw RuntimeException("FragmentStatisticPageBinding = null")

    private var waterIntake: Int = 0
    private var waterGoal: Int = 10
    private var totalSteps: Int = 0

    private val fitnessOptions: FitnessOptions by lazy {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .build()
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var isGoogleFitAuthenticated = false

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                isGoogleFitAuthenticated = true
                accessGoogleFit()
            } else {
                initializeStepCounterSensor()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticPageBinding.inflate(inflater, container, false)
        initializeGoogleSignInClient()
        requestPermissions()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        if (!isGoogleFitAuthenticated) {
            val intent = Intent(requireContext(), StepCounterService::class.java)
            requireActivity().startService(intent)
        }

        binding.cvWaterIntake.setOnClickListener {
            waterIntake += 1
            binding.tvUserWater.text = String.format("$waterIntake / $waterGoal")
            saveWaterIntake()
        }
    }

    private fun initializeGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope(Fitness.SCOPE_ACTIVITY_READ.toString()),
                com.google.android.gms.common.api.Scope(Fitness.SCOPE_LOCATION_READ.toString())
            )
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun requestPermissions() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    val intent = googleSignInClient.signInIntent
                    getResult.launch(intent)
                }
            }
        } else {
            isGoogleFitAuthenticated = true
            accessGoogleFit()
        }
    }

    private fun accessGoogleFit() {
        val account: GoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
            ?: return
        val end = System.currentTimeMillis()
        val start = end - TimeUnit.DAYS.toMillis(1)

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(start, end, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(requireContext(), account)
            .readData(readRequest)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataSets = task.result?.buckets?.flatMap { it.dataSets } ?: emptyList()
                    displayFitnessData(dataSets)
                } else {
                    // Handle error
                }
            }
    }

    private fun displayFitnessData(dataSets: List<DataSet>) {
        var totalSteps = 0
        var totalCalories = 0f
        for (dataSet in dataSets) {
            for (dataPoint in dataSet.dataPoints) {
                for (field in dataPoint.dataType.fields) {
                    when (field.name) {
                        Field.FIELD_STEPS.name -> {
                            val steps = dataPoint.getValue(field).asInt()
                            totalSteps += steps
                        }

                        Field.FIELD_CALORIES.name -> {
                            val calories = dataPoint.getValue(field).asFloat()
                            totalCalories += calories
                        }
                    }
                }
            }
        }
        binding.tvUserSteps.text = totalSteps.toString()
        binding.tvUserCalories.text = totalCalories.toString()
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            FirebaseDatabase.getInstance().getReference("Users").child(it)
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let { setupWaterIntakeGoals(it.gender) }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Обработка ошибок, например, показ сообщения
                    }
                })
        }
    }

    private fun setupWaterIntakeGoals(gender: String?) {
        waterGoal = when (gender) {
            "Female" -> 11
            "Male" -> 15
            else -> 10
        }
        loadWaterIntake()
    }

    private fun loadWaterIntake() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("WaterIntakePrefs", Context.MODE_PRIVATE)
        val savedDate = sharedPreferences.getLong("lastUpdated", 0L)
        val currentDate = System.currentTimeMillis()

        waterIntake =
            if (TimeUnit.MILLISECONDS.toDays(currentDate) > TimeUnit.MILLISECONDS.toDays(savedDate)) {
                0
            } else {
                sharedPreferences.getInt("waterIntake", 0)
            }

        binding.tvUserWater.text = String.format("$waterIntake / $waterGoal")
    }

    private fun saveWaterIntake() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("WaterIntakePrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("waterIntake", waterIntake)
            putLong("lastUpdated", System.currentTimeMillis())
            apply()
        }
    }

    private fun initializeStepCounterSensor() {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            totalSteps = steps
            binding.tvUserSteps.text = totalSteps.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    Log.d("StatisticPageFragment", "Step counter sensor accuracy is high")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    Log.d("StatisticPageFragment", "Step counter sensor accuracy is medium")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    Log.d("StatisticPageFragment", "Step counter sensor accuracy is low")
                }

                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    Log.d("StatisticPageFragment", "Step counter sensor accuracy is unreliable")
                }

                else -> {
                    Log.d("StatisticPageFragment", "Step counter sensor accuracy is unknown")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (!isGoogleFitAuthenticated) {
            sensorManager.unregisterListener(this)
        }
    }

    companion object {
        fun newInstance(): StatisticPageFragment {
            return StatisticPageFragment()
        }
    }
}
