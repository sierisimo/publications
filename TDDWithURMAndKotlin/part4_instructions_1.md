---
title: TDD with URM + Kotlin: Instructions
published: false
description: 
tags: kotlin, tdd
cover_image:
series: TDD With URM and Kotlin
---

Since the last article we achieved our 3 basic operations to have a URM implementation:

- Zero
- Increment
- Jump

With these 3 function we can create more complex instructions based on our _instruction set_ which we already kind of defined… Actually we just created a class for holding the data and the functions just update the data on demand. This can be dangerous on a real time execution, anyone can just go and update the value and mess around with the results or order of execution. We should find a way to avoid this to happen.

