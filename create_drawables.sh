#!/bin/bash
# This script creates all required vector drawable XML files
# Run from the project root: bash create_drawables.sh
# OR just create each file manually in app/src/main/res/drawable/

DRAWABLE_DIR="app/src/main/res/drawable"
mkdir -p "$DRAWABLE_DIR"

# ic_home.xml
cat > "$DRAWABLE_DIR/ic_home.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurfaceVariant"
        android:pathData="M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"/>
</vector>
EOF

# ic_calendar.xml
cat > "$DRAWABLE_DIR/ic_calendar.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurfaceVariant"
        android:pathData="M19,3h-1V1h-2v2H8V1H6v2H5C3.89,3 3.01,3.9 3.01,5L3,19c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,19H5V8h14V19zM7,10h5v5H7z"/>
</vector>
EOF

# ic_alarm.xml
cat > "$DRAWABLE_DIR/ic_alarm.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurfaceVariant"
        android:pathData="M22,5.72l-4.6,-3.86 -1.29,1.53 4.6,3.86L22,5.72zM7.88,3.39L6.6,1.86 2,5.71l1.29,1.53 4.59,-3.85zM12.5,8L11,8v6l4.75,2.85 0.75,-1.23 -4,-2.37V8zM12,4C7.03,4 3,8.03 3,13s4.02,9 9,9c4.97,0 9,-4.03 9,-9S16.97,4 12,4zM12,20c-3.87,0 -7,-3.13 -7,-7s3.13,-7 7,-7 7,3.13 7,7 -3.13,7 -7,7z"/>
</vector>
EOF

# ic_chart.xml
cat > "$DRAWABLE_DIR/ic_chart.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurfaceVariant"
        android:pathData="M19,3H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM9,17H7v-7h2V17zM13,17h-2V7h2V17zM17,17h-2v-4h2V17z"/>
</vector>
EOF

# ic_settings.xml
cat > "$DRAWABLE_DIR/ic_settings.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurfaceVariant"
        android:pathData="M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"/>
</vector>
EOF

# ic_add.xml
cat > "$DRAWABLE_DIR/ic_add.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@android:color/white"
        android:pathData="M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
</vector>
EOF

# ic_food.xml
cat > "$DRAWABLE_DIR/ic_food.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorPrimary"
        android:pathData="M18.06,22.99h1.66c0.84,0 1.53,-0.64 1.63,-1.46L23,5.05h-5V1h-1.97v4.05h-4.97l0.3,2.34c1.71,0.47 3.31,1.32 4.27,2.26 1.44,1.42 2.43,2.89 2.43,5.29v8.05zM1,21.99V21h15.03v0.99c0,0.55 -0.45,1 -1.01,1H2.01c-0.56,0 -1.01,-0.45 -1.01,-1zM16.03,9.13C14.98,8.26 12.5,7.01 9.5,7.01S4.02,8.26 2.97,9.13C1.96,9.97 1,11.32 1,13.01h15.03c0,-1.69 -0.04,-3.04 -1,-3.88z"/>
</vector>
EOF

# ic_medicine.xml
cat > "$DRAWABLE_DIR/ic_medicine.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorSecondary"
        android:pathData="M6.5,10h-2v3h-3v2h3v3h2v-3h3v-2h-3zM19,8l-5.08,-5.08C13.63,2.63 13.14,2.5 12.64,2.5c-0.5,0 -1,0.19 -1.38,0.56L3.5,10.83C3.13,11.2 3,11.7 3,12.2c0,0.5 0.19,1 0.56,1.38L8.64,18.58C9.01,18.95 9.5,19 10,19c0.5,0 1,-0.19 1.38,-0.56l7.76,-7.76C19.68,10.25 19.68,8.43 19,8zM11,17.5l-5.08,-5.08 7.77,-7.77L18.77,9.73 11,17.5z"/>
</vector>
EOF

# ic_check.xml
cat > "$DRAWABLE_DIR/ic_check.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@android:color/white"
        android:pathData="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z"/>
</vector>
EOF

# ic_check_circle.xml
cat > "$DRAWABLE_DIR/ic_check_circle.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/streak_green"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,17l-5,-5 1.41,-1.41L10,14.17l7.59,-7.59L19,8l-9,9z"/>
</vector>
EOF

# ic_snooze.xml
cat > "$DRAWABLE_DIR/ic_snooze.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurfaceVariant"
        android:pathData="M7.88,3.39L6.6,1.86 2,5.71l1.29,1.53 4.59,-3.85zM22,5.72l-4.6,-3.86 -1.29,1.53 4.6,3.86L22,5.72zM12,4C7.03,4 3,8.03 3,13s4.02,9 9,9c4.97,0 9,-4.03 9,-9S16.97,4 12,4zM12,20c-3.87,0 -7,-3.13 -7,-7s3.13,-7 7,-7 7,3.13 7,7 -3.13,7 -7,7zM9,11h3.63L9,15.2V17h6v-2h-3.63L15,10.8V9H9v2z"/>
</vector>
EOF

# ic_dismiss.xml
cat > "$DRAWABLE_DIR/ic_dismiss.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurfaceVariant"
        android:pathData="M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"/>
</vector>
EOF

# ic_missed.xml
cat > "$DRAWABLE_DIR/ic_missed.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/missed_red"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-2h2v2zM13,13h-2V7h2v6z"/>
</vector>
EOF

# ic_pending.xml
cat > "$DRAWABLE_DIR/ic_pending.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/pending_grey"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z"/>
</vector>
EOF

# ic_edit.xml
cat > "$DRAWABLE_DIR/ic_edit.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurfaceVariant"
        android:pathData="M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z"/>
</vector>
EOF

# ic_delete.xml
cat > "$DRAWABLE_DIR/ic_delete.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorError"
        android:pathData="M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"/>
</vector>
EOF

# ic_dot.xml
cat > "$DRAWABLE_DIR/ic_dot.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="6dp" android:height="6dp" android:viewportWidth="6" android:viewportHeight="6">
    <path android:fillColor="@color/streak_green"
        android:pathData="M3,0C1.343,0 0,1.343 0,3S1.343,6 3,6 6,4.657 6,3 4.657,0 3,0Z"/>
</vector>
EOF

# ic_arrow_back.xml
cat > "$DRAWABLE_DIR/ic_arrow_back.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurface"
        android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z"/>
</vector>
EOF

# ic_arrow_forward.xml
cat > "$DRAWABLE_DIR/ic_arrow_forward.xml" << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurface"
        android:pathData="M12,4l-1.41,1.41L16.17,11H4v2h12.17l-5.58,5.59L12,20l8,-8z"/>
</vector>
EOF

echo "✅ All drawable icons created in $DRAWABLE_DIR"
