# Spin-Wheel

🎡 Spin Wheel Module (Java)
A customizable Spin Wheel (Lucky Wheel) UI component for Android, written entirely in Java. Great for gamified apps, rewards, lucky draws, and giveaways — this module provides an engaging and dynamic spinning experience for your users.

✨ Features
Smooth spinning animation with deceleration

Configurable number of segments and colors

Set custom labels, images, or rewards on each segment

Spin with random or predefined target

Callback listener when the wheel stops

Lightweight, easy to integrate

🧩 Integration
Step 1: Add the view in your layout
xml
Copy
Edit
<com.yourpackage.SpinWheelView
    android:id="@+id/spinWheel"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
Step 2: Configure and spin the wheel in Java
java
Copy
Edit
SpinWheelView spinWheel = findViewById(R.id.spinWheel);

// Setup segments
List<String> segments = Arrays.asList("10 Coins", "Try Again", "50 Coins", "100 Coins", "Jackpot");
spinWheel.setSegments(segments);

// Start spin with random or fixed target
spinWheel.spinToIndex(new Random().nextInt(segments.size()));

// Listen for result
spinWheel.setOnSpinCompleteListener(index -> {
    String result = segments.get(index);
    Toast.makeText(this, "You won: " + result, Toast.LENGTH_SHORT).show();
});
⚙️ Customization Options
Segment background colors

Text size and color

Spin duration and speed

Center button or image (optional)

📦 Requirements
Android API 21+

Pure Java (no Kotlin or Compose)

No external dependencies

📄 License
MIT License — free to use and modify in your own apps.
Spin Wheel
