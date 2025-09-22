# RainSeek.Dataset.Milthm

## Build & Data Processing

This project uses Gradle for automated data processing.

To run the full data processing workflow, execute:

```
./gradlew data-process
```

### Common Tasks

- `rename` — Batch rename data (charts, illustrations, people, songs)
- `rename-chart` — Rename chart data
- `rename-illustrations` — Rename illustration data
- `rename-people` — Rename people data
- `rename-songs` — Rename song data
- `data-load` — Load raw data
- `data-saves` — Save processed data to files
- `data-indexing` — Index data into SQLite database
- `benchmark` — Searching benchmark

You can run individual tasks, for example:

```bash
./gradlew rename
./gradlew data-load
./gradlew data-saves
./gradlew data-indexing
```

## Contribution

Contributions are welcome — especially help with tagging or labeling the data!

## Licensing

All **source code** in this repository is licensed under the [MIT License](./LICENSE).  
You are free to use, modify, and distribute the code with minimal restrictions.

However, all **data files** (e.g., CSV, JSON, image, YAML, or audio files) are copyrighted by **Morizero**.  
These files may not be copied, redistributed, or used for any **commercial purposes** without explicit written permission.

If you contribute any data files or annotations via commits, you agree to **waive any copyright claims** to those contributions.  
All rights to contributed data become the property of **the Milthm team**.

Please refer to the full data license in [LICENSE-DATA](./LICENSE-DATA) for more details.

For data licensing inquiries, contact: [copyright+milthm-data-readme@morizero.com](mailto:copyright+milthm-data-readme@morizero.com)
