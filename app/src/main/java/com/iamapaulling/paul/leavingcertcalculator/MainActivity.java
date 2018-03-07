package com.iamapaulling.paul.leavingcertcalculator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    int numRows = 6;
    int subjectLimit = 20;

    // Grades
    int[] gradePointsHigher = {100, 88, 77, 66, 56, 46, 37, 0};
    int[] gradePointsLower = {56, 46, 37, 28, 20, 12, 0, 0};

    final int indexOfMaths = 3;
    String[] subjectNames = {
            "Select Subject...",
            "English", "Irish", "Maths",

            "Languages",
            "French", "German", "Spanish", "Italian", "Japanese", "Russian", "Arabic", "Latin", "Ancient Greek", "Hebrew",

            "Laboratory Sciences",
            "Physics", "Chemistry", "Biology", "Agricultural Science", "Physics and Chemistry", "Applied Mathematics",

            "Business Studies",
            "Accounting", "Agricultural Economics", "Business", "Economics",

            "Applied Sciences",
            "Technology", "Engineering", "Home Economics, Scientific and Social", "Design and Communication Graphics", "Construction Studies",

            "Arts and Humanities",
            "Art", "Classical Studies", "Geography", "History", "Music", "Religious Education",

            "Non-curricular Languages",
            "Bulgarian", "Croatian", "Czech", "Danish", "Dutch", "Estonian", "Finnish", "Greek", "Hungarian", "Latvian", "Lithuanian", "Polish", "Portuguese", "Romanian", "Slovak", "Swedish",
    };

    // Existing view variables
    TextView totalPointsTextView = null;
    Button addLCVPBtn = null;
    Button addSubjectBtn = null;
    LinearLayout scrollingLayoutContainer = null;

    // LCVP Row Stuff
    int LCVPIndex = 0;
    Spinner LCVPSpinnerGrade = null;

    // Subject row arrays to keep reference to each instance
    ArrayList<Spinner> subjectSpinners = new ArrayList<>(numRows);
    ArrayList<ToggleButton> subjectLevels = new ArrayList<>(numRows);
    ArrayList<EditText> subjectGrades = new ArrayList<>(numRows);
    ArrayList<TextView> subjectPoints = new ArrayList<>(numRows);

    // Msc
    LayoutInflater inflater = null;
    ArrayAdapter spinnerAdapter = null;
    int IMEDoneIndex = 0; // Indicates which EditText has IME set to done (i.e. the last one, but LCVP tho)
    boolean restoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Stop keyboard opening on startup
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Check for first run?
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if (!previouslyStarted) {
            // Open about activity on first launch
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.apply();
            MainActivity.this.startActivity(new Intent(MainActivity.this, AboutActivity.class));
        }

        // Set up private member view variables
        // Get Total points TextView
        totalPointsTextView = (TextView) findViewById(R.id.points_total);

        // Set up buttons with listeners
        addSubjectBtn = (Button) findViewById(R.id.add_subject_btn);
        addSubjectBtn.setOnClickListener(addRowOnClick());

        addLCVPBtn = (Button) findViewById(R.id.add_lcvp_btn);
        addLCVPBtn.setOnClickListener(addLCVPRowOnClick());

        // Get scroll view to add calculator rows to
        scrollingLayoutContainer = (LinearLayout) findViewById(R.id.scroll_container);

        // Do inflater stuff
        // Select layout to which each subject row will be added
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Populate the spinner with custom view
        spinnerAdapter = new CustomSpinnerAdapter(this, R.layout.custom_spinner_item, subjectNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Add each calculator row
        for (int i = 0; i < numRows; i++) {
            addCalculatorRow(i, false);
        }

        // Set first 3 spinners to have English, Irish, Maths preselected
        subjectSpinners.get(0).setSelection(1);
        subjectSpinners.get(1).setSelection(2);
        subjectSpinners.get(2).setSelection(3);

        // Set last EditText to have a done action button on the keyboard, for maximum awesomeness
        setLastEdiTextIMEDONE(subjectGrades.size() - 1);
    }


    private void addCalculatorRow(int index, boolean animate) {
        // Creating new view from calculator row which will be inserted into the linearLayout in the scrollLayout in activity_main
        View customView = inflater.inflate(R.layout.calculator_row, scrollingLayoutContainer, false); //http://stackoverflow.com/questions/5026926/making-sense-of-layoutinflater/5027921#5027921

        // Populate arraylists to keep references to each ui element as each one hasn't got an id
        subjectSpinners.add(index, (Spinner) customView.findViewById(R.id.subject_spinner));
        subjectLevels.add(index, (ToggleButton) customView.findViewById(R.id.grade_level_btn));
        subjectGrades.add(index, (EditText) customView.findViewById(R.id.grade_value_input));
        subjectPoints.add(index, (TextView) customView.findViewById(R.id.points_textview));

        // Spinner - Add list items and onSelect listener
        Spinner thisSpinner = subjectSpinners.get(index);
        thisSpinner.setId(100 + index); // Set unique id for state change http://code.hootsuite.com/orientation-changes-on-android/
        thisSpinner.setAdapter(spinnerAdapter);
        thisSpinner.setOnItemSelectedListener(spinnerSelectListener());

        // Toggle - When Higher/Lower toggle pressed, recalculate grade
        ToggleButton thisToggle = subjectLevels.get(index);
        thisToggle.setId(200 + index);
        thisToggle.setOnCheckedChangeListener(gradeLevelToggleListener());

        // EditText - Add onTextChanged listener
        EditText thisEditText = subjectGrades.get(index);
        thisEditText.setId(300 + index);
        thisEditText.addTextChangedListener(gradeChangeListener());

        // Add the newly inflated row into the scrollingView's LinearLayout element
        scrollingLayoutContainer.addView(customView);

        if (animate) {
            Animation bottomUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
            customView.startAnimation(bottomUp);
//        slidingBtnView.setVisibility(View.VISIBLE);
        }
    }

    private void scrollToBottom() {
        // Scroll down list when new subject row/LCVP are added
        final ScrollView scrollView = (ScrollView) findViewById(R.id.subjects_scrollview);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    // Click listeners for buttons on the bottom
    private View.OnClickListener addRowOnClick() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                // Set subject limit of 20
                if (numRows > subjectLimit) {
                    Toast.makeText(MainActivity.this, "Sorry Einstein, subject limit reached", Toast.LENGTH_SHORT).show();
                } else {
                    int thisIndex = numRows;
                    numRows++;

                    addCalculatorRow(thisIndex, true);

                    scrollToBottom();

                    setLastEdiTextIMEDONE(thisIndex);
                }
            }
        };
    }

    private void setLastEdiTextIMEDONE(int index) {
        // Replace old DONE with NEXT, add NEXT to the new one, update IMEIndex
        subjectGrades.get(IMEDoneIndex).setImeOptions(EditorInfo.IME_ACTION_NEXT);
        EditText thisEditText = subjectGrades.get(index);
        thisEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        IMEDoneIndex = index;
    }

    private View.OnClickListener addLCVPRowOnClick() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                if (LCVPIndex < 6) {
                    LCVPIndex = numRows;  // Give LCVPIndex the index of the LCVP row
                    numRows++; // Num rows has just increased. At the bottom because using it as index which is one less than the count

                    addLCVPRow(true);

                    scrollToBottom();

                } else {
                    Toast.makeText(MainActivity.this, "LCVP Already Added", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void addLCVPRow(boolean animate) {
        LinearLayout scrollingLayoutContainer = (LinearLayout) findViewById(R.id.scroll_container);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.calculator_row_lcvp, scrollingLayoutContainer, false);

        LCVPSpinnerGrade = (Spinner) customView.findViewById(R.id.lcvp_grade_spinner);
        LCVPSpinnerGrade.setOnItemSelectedListener(spinnerSelectListener());

        // Fill subject spinner, toggle and grade with placeholder nulls. Simplifies calculating total to have subjectPoints in place
        subjectSpinners.add(LCVPIndex, null);
        subjectLevels.add(LCVPIndex, null);
        subjectGrades.add(LCVPIndex, null);
        subjectPoints.add(LCVPIndex, (TextView) customView.findViewById(R.id.lcvp_points_textview));

        scrollingLayoutContainer.addView(customView);

        if (animate) {
            // Fade in row
            Animation bottomUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
            customView.startAnimation(bottomUp);
        }
    }

    // Calculator row listeners
    // Spinner listener. Handles duplicate Maths by showing a toast and resetting selection to 0
    private AdapterView.OnItemSelectedListener spinnerSelectListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == indexOfMaths) {
                    Spinner thisSpinner = (Spinner) parent;
                    for (int i = 0; i < numRows; i++) {
                        // Avoid LCVP row which does not have a subjectSpinner cause its just LCVP, like
                        if (subjectSpinners.get(i) == null || thisSpinner == subjectSpinners.get(i)) {
                            continue;
                        }

                        if (subjectSpinners.get(i).getSelectedItemPosition() == indexOfMaths) {
                            thisSpinner.setSelection(0, true);
                            Toast.makeText(MainActivity.this, "Maths already selected", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
                calculateTotalPoints();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    // Change total immediately when text if entered
    private TextWatcher gradeChangeListener() {
        return new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalPoints();
            }
        };
    }

    private CompoundButton.OnCheckedChangeListener gradeLevelToggleListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calculateTotalPoints();
            }
        };
    }


    // Update total textView with total points
    private void calculateTotalPoints() {
        if (restoring)
            return;
        Pair[] totalPointsArray = new Pair[numRows];

        for (int i = 0; i < numRows; i++) {
            int thisPoints = 0;

            // Avoid LCVP row which does not have an EditText containing a grade, only a spinner
            if (subjectGrades.get(i) == null) {
                totalPointsArray[i] = new Pair(i, thisPoints);
                continue;
            }

            // If there is a grade in the EditText
            if (!subjectGrades.get(i).getText().toString().equals("")) {
                // Get grade value
                int gradeValue = Integer.parseInt(subjectGrades.get(i).getText().toString());
                int gradeIndex = gradeValue - 1; // Account for array offset

                // Check if grade value is valid
                if (gradeValue < 1 || gradeValue > 8) {
                    subjectGrades.get(i).setText("");
                    totalPointsArray[i] = new Pair(i, thisPoints);
                    continue;
                }

                // Calculate points for this subject
                if (subjectLevels.get(i).isChecked()) // Higher lever
                    thisPoints += gradePointsHigher[gradeIndex];
                else
                    thisPoints += gradePointsLower[gradeIndex];

                // Add 25 if higher maths has a grade value
                if (subjectSpinners.get(i).getSelectedItemPosition() == indexOfMaths && subjectLevels.get(i).isChecked() && gradeValue <= 6)
                    thisPoints += 25;
            }

            // Update points TextView
            subjectPoints.get(i).setText(String.format("%d", thisPoints));

            // Add point to the array
            totalPointsArray[i] = new Pair(i, thisPoints);
        }
        if (LCVPIndex >= 6) {
            int LCVPGrade = LCVPSpinnerGrade.getSelectedItemPosition();
            int thisPoints;
            switch (LCVPGrade) {
                case 0:
                    thisPoints = 28;
                    break;
                case 1:
                    thisPoints = 46;
                    break;
                case 2:
                    thisPoints = 66;
                    break;
                default:
                    thisPoints = 0;
                    break;
            }

            subjectPoints.get(LCVPIndex).setText(String.format("%d", thisPoints));


            totalPointsArray[LCVPIndex] = new Pair(LCVPIndex, thisPoints);
        }

        // Sort array
        Arrays.sort(totalPointsArray);

        int totalPointsValue = 0;

        for (int i = 0; i < 6; i++) {
            // Tot up total
            totalPointsValue += totalPointsArray[i].value;
            // Remove strikethroughs
            TextView thisTV = subjectPoints.get(totalPointsArray[i].index);
            thisTV.setPaintFlags(thisTV.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        for (int i = 6; i < numRows; i++) {
            // Add strikethroughs
            TextView thisTV = subjectPoints.get(totalPointsArray[i].index);
            thisTV.setPaintFlags(thisTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        totalPointsTextView.setText(String.format("%d", totalPointsValue));
    }

    //http://stackoverflow.com/questions/23587314/how-to-sort-an-array-and-keep-track-of-the-index-in-java/23587379#23587379
    public class Pair implements Comparable<Pair> {
        public final int index;
        public final int value;

        public Pair(int index, int value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(@NonNull Pair other) {
            // -1 for descending order
            return -1 * Integer.valueOf(this.value).compareTo(other.value);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If no new rows have been added, dont have to do anything
        if (numRows == 6)
            return;

        int[] spinnerSelection = new int[numRows - 6];
        boolean[] levelSelection = new boolean[numRows - 6];
        String[] gradeSelection = new String[numRows - 6];

        for (int i = 0; i < numRows - 6; i++) {
            if (subjectSpinners.get(i + 6) == null)
                continue;

            spinnerSelection[i] = subjectSpinners.get(i + 6).getSelectedItemPosition();
            levelSelection[i] = subjectLevels.get(i + 6).isChecked();
            gradeSelection[i] = subjectGrades.get(i + 6).getText().toString();
        }

        outState.putIntArray("spinnerSelection", spinnerSelection);
        outState.putBooleanArray("levelSelection", levelSelection);
        outState.putStringArray("gradeSelection", gradeSelection);

        outState.putInt("LCVPIndex", LCVPIndex);
        if (LCVPIndex > 0) {
            outState.putInt("LCVPSpinnerSelection", LCVPSpinnerGrade.getSelectedItemPosition());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // No items saved
        if (savedInstanceState.getIntArray("spinnerSelection") == null)
            return;

        // Get saved rows
        // Tell calc total points to not freak out
        restoring = true;
        int[] spinnerSelection = savedInstanceState.getIntArray("spinnerSelection");
        boolean[] levelSelection = savedInstanceState.getBooleanArray("levelSelection");
        String[] gradeSelection = savedInstanceState.getStringArray("gradeSelection");

        LCVPIndex = savedInstanceState.getInt("LCVPIndex");

        if (spinnerSelection == null || levelSelection == null || gradeSelection == null)
            return;

        for (int i = 0; i < spinnerSelection.length; i++) { // Restore spinner position
            numRows++;

            if (LCVPIndex == i + 6) {
                // Add LCVP row and then set spinner
                addLCVPRow(false);
                LCVPSpinnerGrade.setSelection(savedInstanceState.getInt("LCVPSpinnerSelection"));
                continue;
            }

            addCalculatorRow(i + 6, false);
            setLastEdiTextIMEDONE(i + 6);
            // Set rows
            subjectSpinners.get(i + 6).setSelection(spinnerSelection[i]);
            subjectLevels.get(i + 6).setChecked(levelSelection[i]);
            subjectGrades.get(i + 6).setText(gradeSelection[i]);
        }
        restoring = false;
        calculateTotalPoints();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.about_activity) {
            MainActivity.this.startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_reset) {

            new AlertDialog.Builder(this)
                    .setTitle("Careful")
                    .setMessage("Reset will clear all subjects and grades. There is no undo.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Reset", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = getIntent();
                            finish();
                            overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
