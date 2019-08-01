---
title: TDD with URM + Kotlin: Instructions
published: false
description: 
tags: kotlin, tdd
cover_image:
series: TDD With URM and Kotlin
---

Since the last article we achieved our 3 basic operations

- Zero
- Increment
- Jump

With these 3 function we can create more complex instructions based on our _instruction set_ which we already kind of defined… actually we just created a class for holding the data and the functions just update the data on demand. This can be dangerous on a real time execution, anyone can just go and update the value. We should find a way to avoid this to happen.

## 