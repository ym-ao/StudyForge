#!/usr/bin/env python3
"""Static regression gate for narrow-screen bottom navigation labels."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
source = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
for label in ('Home', 'Library', 'Folders', 'Study', 'Backup', 'Stats', 'Settings'):
    assert f'NavItem("{label}"' in source, f"Missing compact nav label: {label}"
assert 'NavItem("Folders", Icons.Default.Folder, "Collections")' in source
assert 'NavItem("Stats", Icons.Default.Insights, "Progress")' in source
assert 'maxLines = 1' in source and 'softWrap = false' in source
assert 'fontSize = 10.sp' in source
assert 'contentDescription = item.contentDescription' in source
print("PASS: seven compact bottom-navigation labels fit one line and retain full accessibility names")
