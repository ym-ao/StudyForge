# Third-party notices

This file records third-party components included in or used by StudyForge 2.1.2. Keep these notices with source and binary distributions.

## RaTeX Android renderer

StudyForge uses `io.github.erweixin:ratex-android:0.1.15` for native Android LaTeX rendering. The Android view uses a native rendering path and packages its required math fonts with the dependency; formula display does not require fetching a renderer from a remote service.

- Project: https://github.com/erweixin/RaTeX
- Artifact: `io.github.erweixin:ratex-android:0.1.15`
- Library code license: MIT License (copyright holder: erweixin)
- Bundled KaTeX mathematical fonts: SIL Open Font License 1.1; these fonts are **not** covered by the RaTeX root MIT license.

### RaTeX library code — MIT License

```text
MIT License

Copyright (c) erweixin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### KaTeX mathematical fonts — SIL Open Font License 1.1

The RaTeX project identifies these font files as unmodified binaries from the KaTeX project (`https://github.com/KaTeX/KaTeX`). The upstream attribution notice identifies the copyright holders as Design Science, Inc. and Khan Academy, with a per-font Reserved Font Name (for example, `KaTeX_Main`).

```text
Copyright (c) 2009-2010, Design Science, Inc. (<www.mathjax.org>)
Copyright (c) 2014-2018 Khan Academy (<www.khanacademy.org>),
with Reserved Font Name KaTeX_Main.
This Font Software is licensed under the SIL Open Font License, Version 1.1.
This license available with a FAQ at:
http://scripts.sil.org/OFL
```

The full SIL Open Font License, Version 1.1:

```text
SIL OPEN FONT LICENSE

Version 1.1 - 26 February 2007

PREAMBLE

The goals of the Open Font License (OFL) are to stimulate worldwide development
of collaborative font projects, to support the font creation efforts of academic
and linguistic communities, and to provide a free and open framework in which
fonts may be shared and improved in partnership with others.

The OFL allows the licensed fonts to be used, studied, modified and redistributed
freely as long as they are not sold by themselves. The fonts, including any
derivative works, can be bundled, embedded, redistributed and/or sold with any
software provided that any reserved names are not used by derivative works.
The fonts and derivatives, however, cannot be released under any other type of
license. The requirement for fonts to remain under this license does not apply
to any document created using the fonts or their derivatives.

DEFINITIONS

"Font Software" refers to the set of files released by the Copyright Holder(s)
under this license and clearly marked as such. This may include source files,
build scripts and documentation.

"Reserved Font Name" refers to any names specified as such after the copyright
statement(s).

"Original Version" refers to the collection of Font Software components as
distributed by the Copyright Holder(s).

"Modified Version" refers to any derivative made by adding to, deleting, or
substituting — in part or in whole — any of the components of the Original
Version, by changing formats or by porting the Font Software to a new environment.

"Author" refers to any designer, engineer, programmer, technical writer or other
person who contributed to the Font Software.

PERMISSION & CONDITIONS

Permission is hereby granted, free of charge, to any person obtaining a copy of
the Font Software, to use, study, copy, merge, embed, modify, redistribute, and
sell modified and unmodified copies of the Font Software, subject to the following
conditions:

1) Neither the Font Software nor any of its individual components, in Original
or Modified Versions, may be sold by itself.

2) Original or Modified Versions of the Font Software may be bundled,
redistributed and/or sold with any software, provided that each copy contains
the above copyright notice and this license. These can be included either as
stand-alone text files, human-readable headers or in the appropriate
machine-readable metadata fields within text or binary files as long as those
fields can be easily viewed by the user.

3) No Modified Version of the Font Software may use the Reserved Font Name(s)
unless explicit written permission is granted by the corresponding Copyright
Holder. This restriction only applies to the primary font name as presented to
the users.

4) The name(s) of the Copyright Holder(s) or the Author(s) of the Font Software
shall not be used to promote, endorse or advertise any Modified Version, except
to acknowledge the contribution(s) of the Copyright Holder(s) and the Author(s)
or with their explicit written permission.

5) The Font Software, modified or unmodified, in part or in whole, must be
distributed entirely under this license, and must not be distributed under any
other license. The requirement for fonts to remain under this license does not
apply to any document created using the Font Software.

TERMINATION

This license becomes null and void if any of the above conditions are not met.

DISCLAIMER

THE FONT SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF COPYRIGHT, PATENT,
TRADEMARK, OR OTHER RIGHT. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, INCLUDING ANY GENERAL, SPECIAL, INDIRECT,
INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF THE USE OR INABILITY TO USE THE FONT SOFTWARE
OR FROM OTHER DEALINGS IN THE FONT SOFTWARE.
```

Upstream source of the RaTeX font notice and full license: `THIRD_PARTY_NOTICES.txt`, `licenses/KaTeX-fonts-NOTICE.txt`, and `licenses/SIL-OFL-1.1.txt` in the RaTeX repository.

## FSRS-6 algorithm and default parameters

The StudyForge scheduler implements FSRS-6 equations in Kotlin and uses the published 21 default parameter values. FSRS-6 uses Stability, Difficulty, and Retrievability. This is an implementation of the algorithm equations, not a bundled copy of a separate FSRS library. Elapsed review time is counted in whole local study days.

- Algorithm reference: https://github.com/open-spaced-repetition/fsrs4anki/wiki/The-Algorithm
- FSRS project: https://github.com/open-spaced-repetition

The defaults are not optimized against the user's personal review history. StudyForge does not claim official certification or validated equivalence until it passes upstream reference vectors and its own regression tests.

## AndroidX, Jetpack Compose, Room, and Kotlin

StudyForge uses AndroidX/Jetpack Compose, Room, Kotlin, and related components through their Gradle artifacts. Their applicable license texts and notices are published by the Android Open Source Project and the respective upstream projects.
