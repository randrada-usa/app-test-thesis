| Component | Tool/Technology |
|-----------|----------------|
| Mobile Platform | Android (minSdk 26) |
| Programming Languages | Kotlin, Python |
| UI Framework | Jetpack Compose |
| Architecture Pattern | MVVM (ViewModel + Repository) |
| Camera | CameraX |
| Image Preprocessing | Bitmap → 224×224 Resize, [0, 255] Float32 Normalization |
| Dataset Processing | Python (image_dataset_from_directory) |
| AI Framework | TensorFlow / Keras |
| Model Training | TensorFlow / Keras (MobileNetV3 backbone) |
| Mobile AI Runtime | TensorFlow Lite (TFLite) |
| Machine Learning Model | MobileNetV3-Small / MobileNetV3-Large |
| Feature Extraction | Built-in MobileNetV3 CNN (Depthwise Separable Convolutions) |
| Inference Engine | TFLite Interpreter (CPU, 4 threads) |
| Batch Aggregation | Majority Vote + Tie-break by Confidence |
| Local Database | Room (SQLite) |
| Persistence | JSON-serialized predictions in Room |
| IDE | Google Antigravity / Android Studio |
