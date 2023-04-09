This is an Android app that allows the user to count their steps and track their progress towards a daily step goal. It also includes a feature to reset the step count at midnight each day.

The app uses the device's step detector sensor to count the number of steps taken by the user. The step count is displayed in a TextView and is updated in real-time as the user takes steps. The app also includes a reset button to allow the user to reset the step count at any time.

To track progress towards a daily step goal, it displays the user's step goal in a TextView. The default step goal is set to 10,000 steps, but the user can change this value in the app's settings.

To reset the step count at midnight each day, the app uses a Timer and a Handler to schedule a task that runs at midnight each day. When this task runs, it resets the step count to 0 and updates the TextView to display the new value. The app also displays a Toast message to notify the user that the step count has been reset.


