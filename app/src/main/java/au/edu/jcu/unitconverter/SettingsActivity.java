package au.edu.jcu.unitconverter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText stepGoalEditText;

    private SharedPreferences sharedPrefs;
    private static final String STEP_GOAL_KEY = "stepGoal";

    // Gets the MenuInflater for this activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /* This method is called when a user selects a menu option */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        stepGoalEditText = findViewById(R.id.stepGoalEditText);

        sharedPrefs = getSharedPreferences("myPreferences", Context.MODE_PRIVATE); // initialise shared-pref
        int stepGoal = sharedPrefs.getInt(STEP_GOAL_KEY, 10000); //
        stepGoalEditText.setText(String.valueOf(stepGoal)); // retrieve stored step goal and render as string

        // called when done action is completed by user
//
        stepGoalEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) { // action key performs "done" operation when user finished editing text ...
                int stepGoal1 = Integer.parseInt(stepGoalEditText.getText().toString()); // step goal stored as int
                SharedPreferences.Editor editor = sharedPrefs.edit(); // reference shared preferences object
                editor.putInt(STEP_GOAL_KEY, stepGoal1); // stores new step goal value in share pref file using putInt method
                editor.apply(); // applies changes to file using apply method
                return true;
            }
            return false; // method returns false if user hasn't finished editing step goal text
        });
    }
}
