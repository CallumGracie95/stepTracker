/*This is an Android app that allows the user to count their
 steps and track their progress towards a daily step goal.
 It also includes a feature to reset the step count at midnight each day.
 The app uses the device's step detector sensor to count the number of steps taken by the user.*/

package au.edu.jcu.unitconverter;

// imports
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepDetector;
    private TextView stepCount;

    private int steps = 0;

    private SharedPreferences sharedPrefs;
    private static final String STEP_COUNT_KEY = "stepCount";
    private static final String STEP_GOAL_KEY = "stepGoal";
    private final Handler handler = new Handler();

    /* This method defines a runnable instance that resets the step count
     at midnight and schedules itself to be executed again*/
    private final Runnable resetAtMidnight = new Runnable() {
        @Override
        public void run() {
            resetStepCountAtMidnight();
            handler.postDelayed(this, getMillisecondsUntilNextMidnight());
        }
    };
    /* This method is called by the runnable and resets the steps and UI components */
    private void resetStepCountAtMidnight() {
        // Gets the current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Gets the time until the next midnight
        long timeUntilNextMidnight = getMillisecondsUntilNextMidnight();

        // Creates a new timer task to reset the step count
        TimerTask resetStepCountTask = new TimerTask() {
            @Override
            public void run() {
                // Reset the step count and update the UI
                steps = 0;
                runOnUiThread(() -> {
                    stepCount.setText(String.valueOf(steps));
                    Toast.makeText(MainActivity.this, "Step count reset at midnight", Toast.LENGTH_SHORT).show();
                });

                // Schedule the next step count reset
                resetStepCountAtMidnight();
            }
        };

        // Schedules the timer task to run at the next midnight
        Timer timer = new Timer();
        timer.schedule(resetStepCountTask, currentTimeMillis + timeUntilNextMidnight);
    }

    /* Calculates the number of milliseconds from the current time until midnight.
    * By setting the calender object fields to midnight subtracting the current time
    * we can get the time to midnight */
    private long getMillisecondsUntilNextMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long timeUntilNextMidnight = calendar.getTimeInMillis() - System.currentTimeMillis();

        // If the current time is already after midnight, add a day to the time until the next midnight
        if (timeUntilNextMidnight < 0) {
            timeUntilNextMidnight += 24 * 60 * 60 * 1000;
        }

        return timeUntilNextMidnight;
    }


    @Override
    // Gets the MenuInflater for this activity
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflates main_menu resource into menu object
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* This method is called when a user selects a menu option */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // checks menu xml for file with id settings
        if (item.getItemId() == R.id.settings) {
            // creates new intent object to launch SettingsActivity class
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);}

    /* Sets up interface and initialize components */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        stepCount = findViewById(R.id.stepCountTextView); // gets reference to textview
        Button resetButton = findViewById(R.id.resetButton); // gets reference to reset button

        sharedPrefs = getSharedPreferences("myPreferences", Context.MODE_PRIVATE); // store values in object
        steps = sharedPrefs.getInt(STEP_COUNT_KEY, 0); // gets step count and assigns to steps variable
        stepCount.setText(String.valueOf(steps)); // converts int value to string for display

        // declarations
        TextView stepGoal = findViewById(R.id.stepGoalTextView);
        int stepGoalValue = sharedPrefs.getInt(STEP_GOAL_KEY, 10000); // gets value of key from shared preferences object and assigns it to step goal value otherwise returns 10000 by default
        stepGoal.setText("Step Goal: " + stepGoalValue);


        // below code is executed when button is clicked
        resetButton.setOnClickListener(view -> {
            steps = 0; // reset step count
            stepCount.setText(String.valueOf(steps));
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(STEP_COUNT_KEY, steps);
            editor.apply();
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        // if step detector not available on user device display this message
        if (stepDetector == null) {
            stepCount.setText("Step detector sensor not available on this device");
        }
        // Schedule the first step count reset at midnight
        resetStepCountAtMidnight();
        // Schedule the step count reset at midnight every day
        handler.postDelayed(resetAtMidnight, getMillisecondsUntilNextMidnight());
    }

    /* activity lifecycle methods */
    @Override
    protected void onResume() { // called when activity is about to resume
        super.onResume();
        sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL); // activate step detector

        // Reset step count at midnight
        handler.postDelayed(resetAtMidnight, getMillisecondsUntilNextMidnight());
    }

    @Override
    protected void onPause() { // called the activity is going into the background or not visible
        super.onPause();
        sensorManager.unregisterListener(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(STEP_COUNT_KEY, steps); // saves step count
        editor.apply();
    }
    /* checks if the sensor event is for the step detector sensor.
    If it is, it increments the steps variable
    by 1 and updates the stepCount TextView.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        // Check if the event is from the step detector sensor
        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // Increment the step count
            steps++;
            // Update the step count display
            stepCount.setText(String.valueOf(steps));
            // Store the step count in the shared preferences
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(STEP_COUNT_KEY, steps);
            editor.apply();
            // Check if the step count has reached the step goal
            int stepGoalValue = sharedPrefs.getInt(STEP_GOAL_KEY, 10000);
            if (steps >= stepGoalValue) {
                // Show a toast message when the user hits the step count
                Toast.makeText(MainActivity.this, "You've reached your step goal!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { // satisfy requirements of SensorEventListener interface
        // Do nothing
    }
}
